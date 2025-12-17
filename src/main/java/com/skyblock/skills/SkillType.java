package com.skyblock.skills;

import org.bukkit.Material;

/**
 * Enum representing all skill types.
 */
public enum SkillType {

    MINING("Mining", "&b&lMining", Material.DIAMOND_PICKAXE, 60, false),
    FARMING("Farming", "&a&lFarming", Material.GOLDEN_HOE, 60, false),
    COMBAT("Combat", "&c&lCombat", Material.DIAMOND_SWORD, 60, false),
    FORAGING("Foraging", "&2&lForaging", Material.JUNGLE_SAPLING, 50, false),
    FISHING("Fishing", "&9&lFishing", Material.FISHING_ROD, 50, false),
    ENCHANTING("Enchanting", "&d&lEnchanting", Material.ENCHANTING_TABLE, 60, false),
    ALCHEMY("Alchemy", "&5&lAlchemy", Material.BREWING_STAND, 50, false),
    RUNECRAFTING("Runecrafting", "&e&lRunecrafting", Material.MAGMA_CREAM, 25, false),
    SOCIAL("Social", "&f&lSocial", Material.CAKE, 25, true),
    CARPENTRY("Carpentry", "&6&lCarpentry", Material.CRAFTING_TABLE, 50, true),
    TAMING("Taming", "&d&lTaming", Material.BONE, 50, false);

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int maxLevel;
    private final boolean cosmetic;

    SkillType(String id, String displayName, Material icon, int maxLevel, boolean cosmetic) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.cosmetic = cosmetic;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean isCosmetic() {
        return cosmetic;
    }

    /**
     * Get skill type from string ID.
     */
    public static SkillType fromString(String id) {
        if (id == null) return null;
        String normalized = id.toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return SkillType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get the config key for this skill.
     */
    public String getConfigKey() {
        return name().toLowerCase();
    }
}
