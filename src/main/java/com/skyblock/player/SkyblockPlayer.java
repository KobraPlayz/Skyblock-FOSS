package com.skyblock.player;

import com.skyblock.SkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a player in the Skyblock system.
 * Contains profile data and player-specific settings.
 */
public class SkyblockPlayer {

    private final SkyblockPlugin plugin;
    private final UUID uuid;
    private final String username;
    private final long firstJoin;
    private PlayerProfile activeProfile;
    private PlayerStats cachedStats;

    public SkyblockPlayer(SkyblockPlugin plugin, UUID uuid, String username, long firstJoin) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.username = username;
        this.firstJoin = firstJoin;
    }

    /**
     * Get the player's UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the player's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the player's first join timestamp.
     */
    public long getFirstJoin() {
        return firstJoin;
    }

    /**
     * Get the active profile.
     */
    public PlayerProfile getActiveProfile() {
        return activeProfile;
    }

    /**
     * Set the active profile.
     */
    public void setActiveProfile(PlayerProfile profile) {
        this.activeProfile = profile;
        this.cachedStats = null; // Invalidate stats cache
    }

    /**
     * Get the Bukkit Player object.
     */
    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Check if the player is online.
     */
    public boolean isOnline() {
        Player player = getBukkitPlayer();
        return player != null && player.isOnline();
    }

    /**
     * Get the player's purse (coins in inventory).
     */
    public double getPurse() {
        return activeProfile != null ? activeProfile.getPurse() : 0;
    }

    /**
     * Set the player's purse.
     */
    public void setPurse(double amount) {
        if (activeProfile != null) {
            activeProfile.setPurse(amount);
        }
    }

    /**
     * Add coins to the player's purse.
     */
    public void addCoins(double amount) {
        if (activeProfile != null) {
            double maxCoins = plugin.getConfigManager().getConfig().getDouble("economy.max-coins", 999999999999.0);
            double newAmount = Math.min(activeProfile.getPurse() + amount, maxCoins);
            activeProfile.setPurse(newAmount);
        }
    }

    /**
     * Remove coins from the player's purse.
     * Returns true if successful, false if not enough coins.
     */
    public boolean removeCoins(double amount) {
        if (activeProfile != null && activeProfile.getPurse() >= amount) {
            activeProfile.setPurse(activeProfile.getPurse() - amount);
            return true;
        }
        return false;
    }

    /**
     * Check if the player has enough coins.
     */
    public boolean hasCoins(double amount) {
        return activeProfile != null && activeProfile.getPurse() >= amount;
    }

    /**
     * Get the player's bank balance.
     */
    public double getBankBalance() {
        return activeProfile != null ? activeProfile.getBankBalance() : 0;
    }

    /**
     * Get a skill level.
     */
    public int getSkillLevel(String skill) {
        if (activeProfile == null) return 0;
        PlayerProfile.SkillData data = activeProfile.getSkills().get(skill.toLowerCase());
        return data != null ? data.getLevel() : 0;
    }

    /**
     * Get a skill's XP.
     */
    public double getSkillXp(String skill) {
        if (activeProfile == null) return 0;
        PlayerProfile.SkillData data = activeProfile.getSkills().get(skill.toLowerCase());
        return data != null ? data.getXp() : 0;
    }

    /**
     * Add XP to a skill.
     */
    public void addSkillXp(String skill, double xp) {
        if (activeProfile != null) {
            plugin.getSkillManager().addXp(this, skill.toLowerCase(), xp);
        }
    }

    /**
     * Get a collection amount.
     */
    public long getCollectionAmount(String collection) {
        if (activeProfile == null) return 0;
        PlayerProfile.CollectionData data = activeProfile.getCollections().get(collection.toLowerCase());
        return data != null ? data.getAmount() : 0;
    }

    /**
     * Get a collection tier.
     */
    public int getCollectionTier(String collection) {
        if (activeProfile == null) return 0;
        PlayerProfile.CollectionData data = activeProfile.getCollections().get(collection.toLowerCase());
        return data != null ? data.getTier() : 0;
    }

    /**
     * Add to a collection.
     */
    public void addCollection(String collection, long amount) {
        if (activeProfile != null) {
            plugin.getCollectionManager().addCollection(this, collection.toLowerCase(), amount);
        }
    }

    /**
     * Get calculated player stats (cached).
     */
    public PlayerStats getStats() {
        if (cachedStats == null) {
            cachedStats = calculateStats();
        }
        return cachedStats;
    }

    /**
     * Invalidate the stats cache.
     */
    public void invalidateStatsCache() {
        cachedStats = null;
    }

    /**
     * Calculate the player's total stats from all sources.
     * This will be expanded in Phase 2 to include pets, accessories, etc.
     */
    private PlayerStats calculateStats() {
        PlayerStats stats = new PlayerStats();

        // Base stats
        stats.setHealth(100);
        stats.setDefense(0);
        stats.setStrength(0);
        stats.setSpeed(100);
        stats.setIntelligence(100);
        stats.setCritChance(30);
        stats.setCritDamage(50);

        // Add stats from skills
        if (activeProfile != null) {
            for (var entry : activeProfile.getSkills().entrySet()) {
                addSkillStats(stats, entry.getKey(), entry.getValue().getLevel());
            }
        }

        // TODO: Phase 2 - Add stats from:
        // - Equipped armor
        // - Held weapon
        // - Active pet
        // - Accessories
        // - Fairy souls
        // - Potions/buffs

        return stats;
    }

    /**
     * Add stats from skill levels.
     */
    private void addSkillStats(PlayerStats stats, String skill, int level) {
        switch (skill) {
            case "mining":
                stats.addDefense(level);
                stats.addMiningFortune(level * 4);
                break;
            case "farming":
                stats.addHealth(level * 2);
                stats.addFarmingFortune(level * 4);
                break;
            case "combat":
                stats.addCritChance(level * 0.5);
                stats.addStrength(level);
                break;
            case "foraging":
                stats.addStrength(level);
                stats.addForagingFortune(level * 4);
                break;
            case "fishing":
                stats.addHealth(level * 2);
                stats.addSeaCreatureChance(level * 0.2);
                break;
            case "enchanting":
                stats.addIntelligence(level);
                stats.addAbilityDamage(level * 0.5);
                break;
            case "alchemy":
                stats.addIntelligence(level);
                break;
            case "taming":
                stats.addPetLuck(level);
                break;
        }
    }

    /**
     * Get the skill average.
     */
    public double getSkillAverage() {
        if (activeProfile == null) return 0;

        var skills = activeProfile.getSkills();
        if (skills.isEmpty()) return 0;

        double total = 0;
        int count = 0;

        for (var entry : skills.entrySet()) {
            // Don't count cosmetic skills in average
            if (!entry.getKey().equals("social") && !entry.getKey().equals("carpentry")) {
                total += entry.getValue().getLevel();
                count++;
            }
        }

        return count > 0 ? total / count : 0;
    }
}
