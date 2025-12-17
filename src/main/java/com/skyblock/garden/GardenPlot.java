package com.skyblock.garden;

/**
 * Represents a single plot in the garden (96x96 blocks).
 */
public class GardenPlot {

    private final int plotNumber;
    private boolean unlocked;
    private boolean cleaned;
    private String presetType;
    private CropType cropType;

    public GardenPlot(int plotNumber) {
        this.plotNumber = plotNumber;
        this.unlocked = false;
        this.cleaned = false;
        this.presetType = null;
        this.cropType = null;
    }

    public int getPlotNumber() {
        return plotNumber;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isCleaned() {
        return cleaned;
    }

    public void setCleaned(boolean cleaned) {
        this.cleaned = cleaned;
    }

    public String getPresetType() {
        return presetType;
    }

    public void setPresetType(String presetType) {
        this.presetType = presetType;
    }

    public CropType getCropType() {
        return cropType;
    }

    public void setCropType(CropType cropType) {
        this.cropType = cropType;
    }

    /**
     * Get the status of this plot.
     */
    public PlotStatus getStatus() {
        if (!unlocked) return PlotStatus.LOCKED;
        if (!cleaned) return PlotStatus.UNLOCKED;
        if (presetType == null) return PlotStatus.CLEANED;
        return PlotStatus.PRESET_APPLIED;
    }

    /**
     * Get the unlock cost in compost.
     * Cost increases based on plot number.
     */
    public long getUnlockCost() {
        // Base cost + increasing cost per plot
        return 100 + (plotNumber - 1) * 50L;
    }

    /**
     * Get the grid position of this plot (1-24 in a 6x4 or 4x6 grid).
     */
    public int getGridX() {
        return (plotNumber - 1) % 6;
    }

    public int getGridY() {
        return (plotNumber - 1) / 6;
    }

    /**
     * Plot status enum.
     */
    public enum PlotStatus {
        LOCKED("§c", "Locked"),
        UNLOCKED("§e", "Unlocked"),
        CLEANED("§a", "Cleaned"),
        PRESET_APPLIED("§2", "Ready");

        private final String color;
        private final String displayName;

        PlotStatus(String color, String displayName) {
            this.color = color;
            this.displayName = displayName;
        }

        public String getColor() {
            return color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColoredName() {
            return color + displayName;
        }
    }
}
