package com.skyblock.furniture;

import org.bukkit.Material;

/**
 * Types of furniture that can be placed on islands.
 */
public enum FurnitureType {
    // Cosmetic furniture
    TIKI_TORCH("Tiki Torch", FurnitureCategory.COSMETIC, Material.TORCH, "tiki_torch"),
    DECORATIVE_SKULL("Decorative Skull", FurnitureCategory.COSMETIC, Material.SKELETON_SKULL, "decorative_skull"),
    POTTED_PLANT("Potted Plant", FurnitureCategory.COSMETIC, Material.POTTED_FERN, "potted_plant"),
    LANTERN("Lantern", FurnitureCategory.COSMETIC, Material.LANTERN, "lantern"),
    CAMPFIRE("Campfire", FurnitureCategory.COSMETIC, Material.CAMPFIRE, "campfire"),
    BANNER("Banner", FurnitureCategory.COSMETIC, Material.WHITE_BANNER, "banner"),
    ARMOR_DISPLAY("Armor Display", FurnitureCategory.COSMETIC, Material.ARMOR_STAND, "armor_display"),
    PAINTING("Painting", FurnitureCategory.COSMETIC, Material.PAINTING, "painting"),

    // Functional furniture
    CHEST_PLUS("Chest+", FurnitureCategory.FUNCTIONAL, Material.CHEST, "chest_plus"),
    ENCHANTING_TABLE_PLUS("Enchanting Table+", FurnitureCategory.FUNCTIONAL, Material.ENCHANTING_TABLE, "enchanting_plus"),
    CRAFTING_TABLE_PLUS("Crafting Table+", FurnitureCategory.FUNCTIONAL, Material.CRAFTING_TABLE, "crafting_plus"),
    ANVIL_PLUS("Anvil+", FurnitureCategory.FUNCTIONAL, Material.ANVIL, "anvil_plus"),
    FURNACE_PLUS("Furnace+", FurnitureCategory.FUNCTIONAL, Material.FURNACE, "furnace_plus"),
    COMPOSTER("Composter", FurnitureCategory.FUNCTIONAL, Material.COMPOSTER, "composter");

    private final String displayName;
    private final FurnitureCategory category;
    private final Material material;
    private final String modelId; // For resource pack custom models

    FurnitureType(String displayName, FurnitureCategory category, Material material, String modelId) {
        this.displayName = displayName;
        this.category = category;
        this.material = material;
        this.modelId = modelId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FurnitureCategory getCategory() {
        return category;
    }

    public Material getMaterial() {
        return material;
    }

    public String getModelId() {
        return modelId;
    }

    public boolean isCosmetic() {
        return category == FurnitureCategory.COSMETIC;
    }

    public boolean isFunctional() {
        return category == FurnitureCategory.FUNCTIONAL;
    }

    /**
     * Get type from string.
     */
    public static FurnitureType fromString(String name) {
        for (FurnitureType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.modelId.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Furniture categories.
     */
    public enum FurnitureCategory {
        COSMETIC("Cosmetic", "§a"),
        FUNCTIONAL("Functional", "§6");

        private final String displayName;
        private final String color;

        FurnitureCategory(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }

        public String getColoredName() {
            return color + displayName;
        }
    }
}
