package com.skyblock.garden;

import org.bukkit.Material;

/**
 * Types of crops that can be farmed in the garden.
 */
public enum CropType {
    WHEAT("Wheat", Material.WHEAT, 1, 100),
    CARROT("Carrot", Material.CARROTS, 1, 100),
    POTATO("Potato", Material.POTATOES, 1, 100),
    PUMPKIN("Pumpkin", Material.PUMPKIN, 2, 150),
    MELON("Melon", Material.MELON, 2, 150),
    COCOA_BEANS("Cocoa Beans", Material.COCOA, 3, 200),
    CACTUS("Cactus", Material.CACTUS, 3, 200),
    SUGAR_CANE("Sugar Cane", Material.SUGAR_CANE, 4, 250),
    NETHER_WART("Nether Wart", Material.NETHER_WART, 5, 300),
    MUSHROOM("Mushroom", Material.RED_MUSHROOM, 6, 350);

    private final String displayName;
    private final Material material;
    private final int requiredLevel;
    private final int baseUpgradeCost; // Copper cost for first upgrade

    CropType(String displayName, Material material, int requiredLevel, int baseUpgradeCost) {
        this.displayName = displayName;
        this.material = material;
        this.requiredLevel = requiredLevel;
        this.baseUpgradeCost = baseUpgradeCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getBaseUpgradeCost() {
        return baseUpgradeCost;
    }

    /**
     * Get the cost for the next upgrade level.
     * Cost increases with each level.
     */
    public long getUpgradeCost(int currentLevel) {
        // Base cost * (1 + level * 0.5)
        return (long) (baseUpgradeCost * (1 + currentLevel * 0.5));
    }

    /**
     * Get crop type from material.
     */
    public static CropType fromMaterial(Material material) {
        for (CropType crop : values()) {
            if (crop.material == material) {
                return crop;
            }
        }
        return null;
    }

    /**
     * Get crop type from block material (handles crop blocks).
     */
    public static CropType fromBlock(Material blockMaterial) {
        String name = blockMaterial.name();

        if (name.equals("WHEAT") || name.equals("WHEAT_SEEDS")) return WHEAT;
        if (name.equals("CARROTS") || name.equals("CARROT")) return CARROT;
        if (name.equals("POTATOES") || name.equals("POTATO")) return POTATO;
        if (name.equals("PUMPKIN") || name.equals("PUMPKIN_STEM")) return PUMPKIN;
        if (name.equals("MELON") || name.equals("MELON_STEM") || name.equals("MELON_SLICE")) return MELON;
        if (name.equals("COCOA") || name.equals("COCOA_BEANS")) return COCOA_BEANS;
        if (name.equals("CACTUS")) return CACTUS;
        if (name.equals("SUGAR_CANE")) return SUGAR_CANE;
        if (name.equals("NETHER_WART")) return NETHER_WART;
        if (name.contains("MUSHROOM")) return MUSHROOM;

        return null;
    }
}
