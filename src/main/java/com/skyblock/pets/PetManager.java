package com.skyblock.pets;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.skyblock.SkyblockPlugin;
import com.skyblock.api.events.PetLevelUpEvent;
import com.skyblock.api.events.PetSummonEvent;
import com.skyblock.api.events.PetUnsummonEvent;
import com.skyblock.api.events.PetXPGainEvent;
import com.skyblock.player.SkyblockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages all pet-related operations including summoning, XP gain,
 * and persistence.
 *
 * <p>Pets are cached in memory per player and persisted to the database
 * asynchronously. The cache expires after 30 minutes of inactivity.</p>
 *
 * @since Phase 2.0
 * @see Pet
 * @see PetDAO
 */
public class PetManager implements Listener {

    private final SkyblockPlugin plugin;
    private final PetDAO petDAO;
    private final Cache<UUID, List<Pet>> petCache;
    private final Cache<UUID, Pet> activePetCache;

    public PetManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.petDAO = new PetDAO(plugin);

        // Cache for all pets owned by a player
        this.petCache = Caffeine.newBuilder()
                .maximumSize(getConfiguredCacheSize())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        // Separate cache for active pets (more frequently accessed)
        this.activePetCache = Caffeine.newBuilder()
                .maximumSize(getConfiguredCacheSize())
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    // ==================== PUBLIC API ====================

    /**
     * Get all pets owned by a player.
     *
     * @param playerUuid The player's UUID
     * @return List of pets, or empty list if none
     */
    public List<Pet> getPets(UUID playerUuid) {
        // Check module enabled
        if (!isModuleEnabled()) {
            return Collections.emptyList();
        }

        // Try cache first
        List<Pet> cached = petCache.getIfPresent(playerUuid);
        if (cached != null) {
            return new ArrayList<>(cached);
        }

        // Load from database (sync for now, async alternative available)
        List<Pet> loaded = petDAO.loadPetsSync(playerUuid);
        if (!loaded.isEmpty()) {
            petCache.put(playerUuid, loaded);
        }
        return loaded;
    }

    /**
     * Get all pets asynchronously.
     * PREFERRED METHOD for non-critical paths.
     *
     * @param playerUuid The player's UUID
     * @return CompletableFuture with list of pets
     */
    public CompletableFuture<List<Pet>> getPetsAsync(UUID playerUuid) {
        if (!isModuleEnabled()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<Pet> cached = petCache.getIfPresent(playerUuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(new ArrayList<>(cached));
        }

        return petDAO.loadPetsAsync(playerUuid).thenApply(pets -> {
            if (!pets.isEmpty()) {
                petCache.put(playerUuid, pets);
            }
            return pets;
        });
    }

    /**
     * Get the currently active pet for a player.
     *
     * @param playerUuid The player's UUID
     * @return The active pet, or null if no pet is active
     */
    public Pet getActivePet(UUID playerUuid) {
        if (!isModuleEnabled()) {
            return null;
        }

        // Check active pet cache first
        Pet cached = activePetCache.getIfPresent(playerUuid);
        if (cached != null) {
            return cached;
        }

        // Search in all pets
        List<Pet> pets = getPets(playerUuid);
        Pet activePet = pets.stream()
                .filter(Pet::isActive)
                .findFirst()
                .orElse(null);

        if (activePet != null) {
            activePetCache.put(playerUuid, activePet);
        }

        return activePet;
    }

    /**
     * Summon a pet for a player.
     * Unsummons any currently active pet first.
     *
     * @param playerUuid The player's UUID
     * @param pet The pet to summon
     * @return CompletableFuture that completes when summoning is done
     */
    public CompletableFuture<Void> summonPet(UUID playerUuid, Pet pet) {
        if (!isModuleEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            // Get previous pet before unsummoning
            Pet previousPet = getActivePet(playerUuid);

            // Fire event on main thread
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) {
                PetSummonEvent event = new PetSummonEvent(player, pet, previousPet);
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));

                // Check if event was cancelled
                if (event.isCancelled()) {
                    plugin.debug("Pet summon cancelled by event for player " + playerUuid);
                    return;
                }
            }

            // Unsummon current pet first
            unsummonPetSync(playerUuid);

            // Set new pet as active
            pet.setActive(true);
            activePetCache.put(playerUuid, pet);

            // Save to database
            petDAO.savePetSync(pet);

            // Invalidate stats cache for player
            SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(playerUuid);
            if (sbPlayer != null) {
                sbPlayer.invalidateStatsCache();
            }

            plugin.debug("Pet summoned: " + pet.getType() + " for player " + playerUuid);
        });
    }

    /**
     * Unsummon the active pet for a player.
     *
     * @param playerUuid The player's UUID
     * @return CompletableFuture that completes when unsummoning is done
     */
    public CompletableFuture<Void> unsummonPet(UUID playerUuid) {
        if (!isModuleEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> unsummonPetSync(playerUuid));
    }

    /**
     * Grant XP to the active pet of a specific type.
     * Called when a player gains skill XP.
     *
     * @param playerUuid The player's UUID
     * @param skillType The skill type that granted XP
     * @param skillXp The amount of skill XP gained
     */
    public void grantPetXP(UUID playerUuid, String skillType, double skillXp) {
        if (!isModuleEnabled()) {
            return;
        }

        Pet activePet = getActivePet(playerUuid);
        if (activePet == null) {
            return;
        }

        // Check if pet type matches skill
        PetType petType = activePet.getType();
        if (petType.getRelatedSkill() == null) {
            return; // Special pets don't gain XP from skills
        }

        if (!petType.getRelatedSkill().getConfigKey().equalsIgnoreCase(skillType)) {
            return; // Pet doesn't match skill type
        }

        // Calculate pet XP (configurable multiplier)
        double petXpMultiplier = getXpMultiplier();
        double petXp = skillXp * petXpMultiplier;

        // Fire XP gain event (allows plugins to modify XP)
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            PetXPGainEvent xpEvent = new PetXPGainEvent(player, activePet, skillType, petXp);
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(xpEvent));

            // Check if event was cancelled or XP was modified
            if (xpEvent.isCancelled()) {
                plugin.debug("Pet XP gain cancelled by event for player " + playerUuid);
                return;
            }
            petXp = xpEvent.getAmount();
        }

        // Add XP to pet
        int oldLevel = activePet.getLevel();
        boolean leveled = activePet.addXp(petXp);

        // Save async
        petDAO.savePetAsync(activePet);

        // Fire level up event if leveled
        if (leveled && player != null) {
            int newLevel = activePet.getLevel();
            PetLevelUpEvent levelEvent = new PetLevelUpEvent(player, activePet, oldLevel, newLevel);
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(levelEvent));

            plugin.debug("Pet leveled up: " + petType + " " + oldLevel + " -> " + newLevel);
        }

        // Invalidate stats cache
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(playerUuid);
        if (sbPlayer != null) {
            sbPlayer.invalidateStatsCache();
        }
    }

    /**
     * Create a new pet for a player.
     *
     * @param profileId The profile ID
     * @param type The pet type
     * @param rarity The pet rarity
     * @return CompletableFuture with the created pet
     */
    public CompletableFuture<Pet> createPet(int profileId, PetType type, PetRarity rarity) {
        if (!isModuleEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            Pet pet = new Pet(profileId, type, rarity);
            petDAO.savePetSync(pet);

            // Invalidate cache to force reload
            // We don't have direct UUID here, so cache will reload on next access

            plugin.debug("Created new pet: " + type + " (" + rarity + ") for profile " + profileId);
            return pet;
        });
    }

    /**
     * Delete a pet.
     *
     * @param petId The pet ID
     * @return CompletableFuture that completes when deletion is done
     */
    public CompletableFuture<Void> deletePet(int petId) {
        if (!isModuleEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        return petDAO.deletePetAsync(petId).thenRun(() -> {
            // Invalidate all caches
            petCache.invalidateAll();
            activePetCache.invalidateAll();
        });
    }

    // ==================== LIFECYCLE ====================

    /**
     * Called on plugin shutdown.
     * Save all cached data and cleanup.
     */
    public void shutdown() {
        plugin.log(Level.INFO, "Shutting down PetManager...");

        // Save all cached pets
        petCache.asMap().values().forEach(pets ->
            pets.forEach(petDAO::savePetSync)
        );

        // Clear caches
        petCache.invalidateAll();
        activePetCache.invalidateAll();

        plugin.log(Level.INFO, "PetManager shutdown complete.");
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Unsummon pet synchronously (internal use).
     */
    private void unsummonPetSync(UUID playerUuid) {
        Pet activePet = getActivePet(playerUuid);
        if (activePet != null) {
            // Fire event on main thread
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) {
                PetUnsummonEvent event = new PetUnsummonEvent(player, activePet);
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));

                // Check if event was cancelled
                if (event.isCancelled()) {
                    plugin.debug("Pet unsummon cancelled by event for player " + playerUuid);
                    return;
                }
            }

            activePet.setActive(false);
            activePetCache.invalidate(playerUuid);
            petDAO.savePetSync(activePet);

            // Invalidate stats cache
            SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(playerUuid);
            if (sbPlayer != null) {
                sbPlayer.invalidateStatsCache();
            }

            plugin.debug("Pet unsummoned for player " + playerUuid);
        }
    }

    /**
     * Check if pets module is enabled.
     */
    private boolean isModuleEnabled() {
        return plugin.getModuleManager().isModuleEnabled("pets");
    }

    /**
     * Get configured cache size.
     */
    private int getConfiguredCacheSize() {
        return plugin.getConfigManager()
                .getConfig()
                .getInt("pets.cache.size", 1000);
    }

    /**
     * Get configured XP multiplier.
     */
    private double getXpMultiplier() {
        return plugin.getConfigManager()
                .getConfig()
                .getInt("pets.xp-multiplier", 1.0);
    }
}
