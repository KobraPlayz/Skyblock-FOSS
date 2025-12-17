package com.skyblock.garden;

import java.util.*;

/**
 * Represents a player's garden.
 */
public class Garden {

    private final UUID id;
    private final int profileId;
    private String worldName;

    // Progression
    private int gardenLevel;
    private double gardenXp;

    // Currencies
    private long copperBalance;
    private long compostBalance;

    // Timestamps
    private long unlockedAt;

    // Plots (24 total)
    private final Map<Integer, GardenPlot> plots;

    // Crop upgrades
    private final Map<CropType, Integer> cropUpgrades;

    // Milestones
    private final Map<CropType, CropMilestone> milestones;

    // Active visitors
    private final List<GardenVisitor> activeVisitors;

    public Garden(UUID id, int profileId) {
        this.id = id;
        this.profileId = profileId;
        this.gardenLevel = 1;
        this.gardenXp = 0;
        this.copperBalance = 0;
        this.compostBalance = 0;
        this.unlockedAt = System.currentTimeMillis();
        this.plots = new HashMap<>();
        this.cropUpgrades = new EnumMap<>(CropType.class);
        this.milestones = new EnumMap<>(CropType.class);
        this.activeVisitors = new ArrayList<>();

        // Initialize all 24 plots
        for (int i = 1; i <= 24; i++) {
            plots.put(i, new GardenPlot(i));
        }

        // Plot 1 is unlocked by default
        plots.get(1).setUnlocked(true);

        // Initialize crop upgrades and milestones
        for (CropType crop : CropType.values()) {
            cropUpgrades.put(crop, 0);
            milestones.put(crop, new CropMilestone(crop));
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public int getProfileId() {
        return profileId;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getGardenLevel() {
        return gardenLevel;
    }

    public void setGardenLevel(int gardenLevel) {
        this.gardenLevel = gardenLevel;
    }

    public double getGardenXp() {
        return gardenXp;
    }

    public void setGardenXp(double gardenXp) {
        this.gardenXp = gardenXp;
    }

    public long getCopperBalance() {
        return copperBalance;
    }

    public void setCopperBalance(long copperBalance) {
        this.copperBalance = copperBalance;
    }

    public long getCompostBalance() {
        return compostBalance;
    }

    public void setCompostBalance(long compostBalance) {
        this.compostBalance = compostBalance;
    }

    public long getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(long unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    // Currency operations
    public void addCopper(long amount) {
        this.copperBalance += amount;
    }

    public boolean removeCopper(long amount) {
        if (copperBalance >= amount) {
            copperBalance -= amount;
            return true;
        }
        return false;
    }

    public void addCompost(long amount) {
        this.compostBalance += amount;
    }

    public boolean removeCompost(long amount) {
        if (compostBalance >= amount) {
            compostBalance -= amount;
            return true;
        }
        return false;
    }

    // XP operations
    public void addGardenXp(double amount) {
        this.gardenXp += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        // Simple level up formula - can be adjusted
        int newLevel = (int) Math.floor(Math.sqrt(gardenXp / 100)) + 1;
        if (newLevel > gardenLevel) {
            gardenLevel = newLevel;
        }
    }

    // Plot operations
    public Map<Integer, GardenPlot> getPlots() {
        return Collections.unmodifiableMap(plots);
    }

    public GardenPlot getPlot(int plotNumber) {
        return plots.get(plotNumber);
    }

    public int getUnlockedPlotCount() {
        return (int) plots.values().stream().filter(GardenPlot::isUnlocked).count();
    }

    public boolean unlockPlot(int plotNumber, long compostCost) {
        GardenPlot plot = plots.get(plotNumber);
        if (plot == null || plot.isUnlocked()) {
            return false;
        }

        if (removeCompost(compostCost)) {
            plot.setUnlocked(true);
            return true;
        }
        return false;
    }

    // Crop upgrade operations
    public Map<CropType, Integer> getCropUpgrades() {
        return Collections.unmodifiableMap(cropUpgrades);
    }

    public int getCropUpgradeLevel(CropType crop) {
        return cropUpgrades.getOrDefault(crop, 0);
    }

    public boolean upgradeCrop(CropType crop, long copperCost) {
        int currentLevel = getCropUpgradeLevel(crop);
        if (currentLevel >= 9) { // Max level is 9
            return false;
        }

        if (removeCopper(copperCost)) {
            cropUpgrades.put(crop, currentLevel + 1);
            return true;
        }
        return false;
    }

    /**
     * Get farming fortune bonus from crop upgrades.
     * Each upgrade level gives +5 Farming Fortune.
     */
    public int getCropFarmingFortune(CropType crop) {
        return getCropUpgradeLevel(crop) * 5;
    }

    /**
     * Get total farming fortune from all upgrades.
     */
    public int getTotalFarmingFortune() {
        int total = 0;
        for (int level : cropUpgrades.values()) {
            total += level * 5;
        }
        return total;
    }

    // Milestone operations
    public Map<CropType, CropMilestone> getMilestones() {
        return Collections.unmodifiableMap(milestones);
    }

    public CropMilestone getMilestone(CropType crop) {
        return milestones.get(crop);
    }

    public void addCropHarvested(CropType crop, long amount) {
        CropMilestone milestone = milestones.get(crop);
        if (milestone != null) {
            milestone.addAmount(amount);
        }
    }

    // Visitor operations
    public List<GardenVisitor> getActiveVisitors() {
        return Collections.unmodifiableList(activeVisitors);
    }

    public void addVisitor(GardenVisitor visitor) {
        if (activeVisitors.size() < 3) { // Max 3 visitors at a time
            activeVisitors.add(visitor);
        }
    }

    public void removeVisitor(GardenVisitor visitor) {
        activeVisitors.remove(visitor);
    }

    public void clearExpiredVisitors() {
        long now = System.currentTimeMillis();
        activeVisitors.removeIf(v -> v.getExpiresAt() < now);
    }

    /**
     * Check if a crop type can be farmed at the current garden level.
     */
    public boolean canFarmCrop(CropType crop) {
        return gardenLevel >= crop.getRequiredLevel();
    }

    /**
     * Inner class for crop milestones.
     */
    public static class CropMilestone {
        private final CropType cropType;
        private long amountFarmed;
        private int currentTier;

        // Milestone tiers (amount required for each)
        private static final long[] TIER_REQUIREMENTS = {
            100, 500, 1500, 5000, 15000, 50000, 150000, 500000, 1500000, 5000000
        };

        public CropMilestone(CropType cropType) {
            this.cropType = cropType;
            this.amountFarmed = 0;
            this.currentTier = 0;
        }

        public CropType getCropType() {
            return cropType;
        }

        public long getAmountFarmed() {
            return amountFarmed;
        }

        public void setAmountFarmed(long amountFarmed) {
            this.amountFarmed = amountFarmed;
            updateTier();
        }

        public int getCurrentTier() {
            return currentTier;
        }

        public void setCurrentTier(int currentTier) {
            this.currentTier = currentTier;
        }

        public void addAmount(long amount) {
            this.amountFarmed += amount;
            updateTier();
        }

        private void updateTier() {
            for (int i = TIER_REQUIREMENTS.length - 1; i >= 0; i--) {
                if (amountFarmed >= TIER_REQUIREMENTS[i]) {
                    currentTier = i + 1;
                    return;
                }
            }
            currentTier = 0;
        }

        public long getNextTierRequirement() {
            if (currentTier >= TIER_REQUIREMENTS.length) {
                return -1; // Max tier reached
            }
            return TIER_REQUIREMENTS[currentTier];
        }

        public double getProgressToNextTier() {
            long nextReq = getNextTierRequirement();
            if (nextReq == -1) return 1.0;

            long prevReq = currentTier > 0 ? TIER_REQUIREMENTS[currentTier - 1] : 0;
            return (double) (amountFarmed - prevReq) / (nextReq - prevReq);
        }
    }
}
