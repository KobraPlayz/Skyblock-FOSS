package com.skyblock.items;

/**
 * Enum representing item categories.
 */
public enum ItemCategory {

    WEAPON("SWORD", true, false, false),
    BOW("BOW", true, false, false),
    ARMOR("", false, true, false),
    HELMET("HELMET", false, true, false),
    CHESTPLATE("CHESTPLATE", false, true, false),
    LEGGINGS("LEGGINGS", false, true, false),
    BOOTS("BOOTS", false, true, false),
    TOOL("TOOL", false, false, true),
    PICKAXE("PICKAXE", false, false, true),
    AXE("AXE", false, false, true),
    HOE("HOE", false, false, true),
    SHOVEL("SHOVEL", false, false, true),
    FISHING_ROD("FISHING ROD", false, false, true),
    ACCESSORY("ACCESSORY", false, false, false),
    CONSUMABLE("CONSUMABLE", false, false, false),
    MATERIAL("MATERIAL", false, false, false),
    PET_ITEM("PET ITEM", false, false, false),
    MINION("MINION", false, false, false),
    REFORGE_STONE("REFORGE STONE", false, false, false),
    MISC("", false, false, false);

    private final String displayName;
    private final boolean isWeapon;
    private final boolean isArmor;
    private final boolean isTool;

    ItemCategory(String displayName, boolean isWeapon, boolean isArmor, boolean isTool) {
        this.displayName = displayName;
        this.isWeapon = isWeapon;
        this.isArmor = isArmor;
        this.isTool = isTool;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isWeapon() {
        return isWeapon;
    }

    public boolean isArmor() {
        return isArmor;
    }

    public boolean isTool() {
        return isTool;
    }

    /**
     * Check if this category can be reforged.
     */
    public boolean canBeReforged() {
        return isWeapon || isArmor || isTool || this == ACCESSORY;
    }

    /**
     * Parse a category from string.
     */
    public static ItemCategory fromString(String name) {
        if (name == null) return MISC;

        String normalized = name.toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return ItemCategory.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Try partial match
            for (ItemCategory cat : values()) {
                if (cat.name().contains(normalized) || normalized.contains(cat.name())) {
                    return cat;
                }
            }
            return MISC;
        }
    }

    /**
     * Get the reforge type for this category.
     */
    public String getReforgeType() {
        if (isWeapon) return "weapon";
        if (isArmor) return "armor";
        if (isTool) return "tool";
        if (this == ACCESSORY) return "accessory";
        return null;
    }
}
