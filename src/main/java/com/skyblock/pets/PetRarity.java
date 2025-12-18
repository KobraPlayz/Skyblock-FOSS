package com.skyblock.pets;

/**
 * Enum representing pet rarities.
 * Higher rarities provide stronger stat bonuses.
 */
public enum PetRarity {
    COMMON("Common", "&f", 0, 1.0),
    UNCOMMON("Uncommon", "&a", 6, 1.1),
    RARE("Rare", "&9", 11, 1.2),
    EPIC("Epic", "&5", 16, 1.3),
    LEGENDARY("Legendary", "&6", 20, 1.5),
    MYTHIC("Mythic", "&d", 25, 1.8);

    private final String displayName;
    private final String color;
    private final int magicalPower;
    private final double statMultiplier;

    PetRarity(String displayName, String color, int magicalPower, double statMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.magicalPower = magicalPower;
        this.statMultiplier = statMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public int getMagicalPower() {
        return magicalPower;
    }

    public double getStatMultiplier() {
        return statMultiplier;
    }

    /**
     * Get the colored display name.
     */
    public String getColoredName() {
        return color + displayName;
    }

    /**
     * Get the next rarity tier (for tier boost).
     * Returns null if already at maximum.
     */
    public PetRarity getNextTier() {
        int currentIndex = this.ordinal();
        if (currentIndex < values().length - 1) {
            return values()[currentIndex + 1];
        }
        return null;
    }

    /**
     * Parse rarity from string.
     */
    public static PetRarity fromString(String name) {
        for (PetRarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(name) || rarity.displayName.equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return COMMON;
    }
}
