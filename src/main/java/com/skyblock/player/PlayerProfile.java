package com.skyblock.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's profile containing all progression data.
 * Players can have multiple profiles.
 */
public class PlayerProfile {

    private final int id;
    private final UUID playerUuid;
    private final String name;
    private final long createdAt;

    // Economy
    private double purse;
    private double bankBalance;

    // Skills
    private final Map<String, SkillData> skills;

    // Collections
    private final Map<String, CollectionData> collections;

    // Phase 2+ fields (prepared but not used)
    private Object activePet; // Will be Pet in Phase 2
    private Object accessoryBag; // Will be AccessoryBag in Phase 2
    private Object[] backpacks; // Will be Backpack[] in Phase 2

    public PlayerProfile(int id, UUID playerUuid, String name, long createdAt) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.name = name;
        this.createdAt = createdAt;
        this.purse = 0;
        this.bankBalance = 0;
        this.skills = new HashMap<>();
        this.collections = new HashMap<>();
    }

    // Getters
    public int getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public double getPurse() {
        return purse;
    }

    public void setPurse(double purse) {
        this.purse = purse;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }

    public Map<String, SkillData> getSkills() {
        return skills;
    }

    public Map<String, CollectionData> getCollections() {
        return collections;
    }

    /**
     * Set skill data for this profile.
     */
    public void setSkillData(String skill, double xp, int level) {
        skills.put(skill.toLowerCase(), new SkillData(xp, level));
    }

    /**
     * Get skill data.
     */
    public SkillData getSkillData(String skill) {
        return skills.get(skill.toLowerCase());
    }

    /**
     * Set collection data for this profile.
     */
    public void setCollectionData(String item, long amount, int tier) {
        collections.put(item.toLowerCase(), new CollectionData(amount, tier));
    }

    /**
     * Get collection data.
     */
    public CollectionData getCollectionData(String item) {
        return collections.get(item.toLowerCase());
    }

    /**
     * Get total wealth (purse + bank).
     */
    public double getTotalWealth() {
        return purse + bankBalance;
    }

    /**
     * Inner class for skill data.
     */
    public static class SkillData {
        private double xp;
        private int level;

        public SkillData(double xp, int level) {
            this.xp = xp;
            this.level = level;
        }

        public double getXp() {
            return xp;
        }

        public void setXp(double xp) {
            this.xp = xp;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    /**
     * Inner class for collection data.
     */
    public static class CollectionData {
        private long amount;
        private int tier;

        public CollectionData(long amount, int tier) {
            this.amount = amount;
            this.tier = tier;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public int getTier() {
            return tier;
        }

        public void setTier(int tier) {
            this.tier = tier;
        }
    }
}
