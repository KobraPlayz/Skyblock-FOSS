package com.skyblock.collections;

import com.skyblock.SkyblockPlugin;
import com.skyblock.api.events.CollectionUnlockEvent;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages the collections system.
 */
public class CollectionManager implements Listener {

    private final SkyblockPlugin plugin;
    private final Map<String, Collection> collections;
    private final Map<String, CollectionCategory> categories;

    public CollectionManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.collections = new LinkedHashMap<>();
        this.categories = new LinkedHashMap<>();

        loadCollections();
    }

    /**
     * Load collections from configuration.
     */
    private void loadCollections() {
        FileConfiguration config = plugin.getConfigManager().getCollectionsConfig();
        if (config == null) {
            plugin.log(Level.WARNING, "Collections config not found!");
            return;
        }

        // Load categories
        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String categoryId : categoriesSection.getKeys(false)) {
                ConfigurationSection catSection = categoriesSection.getConfigurationSection(categoryId);
                if (catSection != null) {
                    CollectionCategory category = new CollectionCategory(
                            categoryId,
                            catSection.getString("display-name", categoryId),
                            catSection.getString("icon", "STONE"),
                            catSection.getString("description", "")
                    );
                    categories.put(categoryId.toLowerCase(), category);
                }
            }
        }

        // Load collections
        ConfigurationSection collectionsSection = config.getConfigurationSection("collections");
        if (collectionsSection != null) {
            for (String collectionId : collectionsSection.getKeys(false)) {
                ConfigurationSection colSection = collectionsSection.getConfigurationSection(collectionId);
                if (colSection != null) {
                    Collection collection = loadCollection(collectionId, colSection);
                    if (collection != null) {
                        collections.put(collectionId.toLowerCase(), collection);
                    }
                }
            }
        }

        plugin.log(Level.INFO, "Loaded " + categories.size() + " collection categories and " + collections.size() + " collections.");
    }

    /**
     * Load a single collection from config.
     */
    private Collection loadCollection(String id, ConfigurationSection section) {
        String categoryId = section.getString("category", "farming");
        CollectionCategory category = categories.get(categoryId.toLowerCase());

        Collection collection = new Collection(
                id,
                section.getString("display-name", id),
                section.getString("icon", "STONE"),
                category
        );

        // Load tiers
        ConfigurationSection tiersSection = section.getConfigurationSection("tiers");
        if (tiersSection != null) {
            for (String tierStr : tiersSection.getKeys(false)) {
                try {
                    int tier = Integer.parseInt(tierStr);
                    ConfigurationSection tierSection = tiersSection.getConfigurationSection(tierStr);
                    if (tierSection != null) {
                        long requirement = tierSection.getLong("requirement", 0);
                        List<CollectionReward> rewards = new ArrayList<>();

                        List<Map<?, ?>> rewardsList = tierSection.getMapList("rewards");
                        for (Map<?, ?> rewardMap : rewardsList) {
                            String type = (String) rewardMap.get("type");
                            String item = (String) rewardMap.get("item");
                            String description = (String) rewardMap.get("description");
                            rewards.add(new CollectionReward(type, item, description));
                        }

                        collection.addTier(tier, new CollectionTier(tier, requirement, rewards));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return collection;
    }

    /**
     * Register collection listeners.
     */
    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Add to a player's collection.
     */
    public void addCollection(SkyblockPlayer player, String collectionId, long amount) {
        if (amount <= 0) return;

        PlayerProfile profile = player.getActiveProfile();
        if (profile == null) return;

        // Check if collection exists
        Collection collection = collections.get(collectionId.toLowerCase());
        if (collection == null) return;

        // Check if collection category is enabled
        if (collection.getCategory() != null) {
            String categoryId = collection.getCategory().getId();
            if (!plugin.getModuleManager().isSubModuleEnabled("collections", categoryId)) return;
        }

        PlayerProfile.CollectionData data = profile.getCollectionData(collectionId);
        if (data == null) {
            profile.setCollectionData(collectionId, 0, 0);
            data = profile.getCollectionData(collectionId);
        }

        long currentAmount = data.getAmount();
        long newAmount = currentAmount + amount;
        int currentTier = data.getTier();

        data.setAmount(newAmount);

        // Check for tier unlocks
        int newTier = calculateTier(collection, newAmount);
        if (newTier > currentTier) {
            data.setTier(newTier);
            onTierUnlock(player, collection, currentTier, newTier);
        }
    }

    /**
     * Calculate the tier for a collection amount.
     */
    private int calculateTier(Collection collection, long amount) {
        int tier = 0;
        for (CollectionTier ct : collection.getTiers().values()) {
            if (amount >= ct.getRequirement()) {
                tier = ct.getTier();
            } else {
                break;
            }
        }
        return tier;
    }

    /**
     * Handle tier unlock.
     */
    private void onTierUnlock(SkyblockPlayer player, Collection collection, int oldTier, int newTier) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) return;

        // Fire event
        CollectionUnlockEvent event = new CollectionUnlockEvent(bukkitPlayer, collection.getId(), oldTier, newTier);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        // Send message
        String message = plugin.getConfigManager().getRawMessage("collections.tier-unlock")
                .replace("{collection}", collection.getDisplayName())
                .replace("{prev}", NumberUtils.intToRoman(oldTier))
                .replace("{new}", NumberUtils.intToRoman(newTier));
        bukkitPlayer.sendMessage(ColorUtils.colorize(message));

        // Play sound
        if (plugin.getModuleManager().isModuleEnabled("sounds")) {
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        }

        // Show rewards for unlocked tiers
        for (int tier = oldTier + 1; tier <= newTier; tier++) {
            CollectionTier ct = collection.getTier(tier);
            if (ct != null) {
                for (CollectionReward reward : ct.getRewards()) {
                    String rewardMsg = plugin.getConfigManager().getRawMessage("collections.recipe-unlock")
                            .replace("{recipe}", reward.getDescription());
                    bukkitPlayer.sendMessage(ColorUtils.colorize(rewardMsg));
                }
            }
        }
    }

    /**
     * Get a collection by ID.
     */
    public Collection getCollection(String id) {
        return collections.get(id.toLowerCase());
    }

    /**
     * Get all collections.
     */
    public java.util.Collection<Collection> getAllCollections() {
        return collections.values();
    }

    /**
     * Get collections by category.
     */
    public List<Collection> getCollectionsByCategory(String categoryId) {
        List<Collection> result = new ArrayList<>();
        CollectionCategory category = categories.get(categoryId.toLowerCase());
        if (category == null) return result;

        for (Collection col : collections.values()) {
            if (col.getCategory() != null && col.getCategory().getId().equalsIgnoreCase(categoryId)) {
                result.add(col);
            }
        }
        return result;
    }

    /**
     * Get all categories.
     */
    public java.util.Collection<CollectionCategory> getAllCategories() {
        return categories.values();
    }

    /**
     * Get a category by ID.
     */
    public CollectionCategory getCategory(String id) {
        return categories.get(id.toLowerCase());
    }

    /**
     * Get the next tier requirement for a collection.
     */
    public long getNextTierRequirement(Collection collection, int currentTier) {
        CollectionTier nextTier = collection.getTier(currentTier + 1);
        return nextTier != null ? nextTier.getRequirement() : -1;
    }

    /**
     * Get progress to next tier as percentage.
     */
    public double getProgressToNextTier(Collection collection, long currentAmount, int currentTier) {
        CollectionTier current = collection.getTier(currentTier);
        CollectionTier next = collection.getTier(currentTier + 1);

        if (next == null) return 100.0; // Max tier

        long currentReq = current != null ? current.getRequirement() : 0;
        long nextReq = next.getRequirement();

        if (nextReq <= currentReq) return 100.0;

        double progress = (currentAmount - currentReq) / (double) (nextReq - currentReq) * 100;
        return Math.min(100.0, Math.max(0.0, progress));
    }

    /**
     * Reload collections.
     */
    public void reload() {
        collections.clear();
        categories.clear();
        loadCollections();
    }

    // Event handler for item pickup
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!plugin.getModuleManager().isModuleEnabled("collections")) return;

        Player player = (Player) event.getEntity();
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return;

        String itemType = event.getItem().getItemStack().getType().name().toLowerCase();
        int amount = event.getItem().getItemStack().getAmount();

        // Check if there's a collection for this item
        Collection collection = collections.get(itemType);
        if (collection != null) {
            addCollection(sbPlayer, itemType, amount);
        }
    }
}
