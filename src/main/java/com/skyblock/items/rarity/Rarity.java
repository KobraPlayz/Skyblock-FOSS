package com.skyblock.items.rarity;

import net.md_5.bungee.api.ChatColor;

/**
 * Enum representing item rarities.
 */
public enum Rarity {

    COMMON("COMMON", "&f", ChatColor.WHITE, 0),
    UNCOMMON("UNCOMMON", "&a", ChatColor.GREEN, 1),
    RARE("RARE", "&9", ChatColor.BLUE, 2),
    EPIC("EPIC", "&5", ChatColor.DARK_PURPLE, 3),
    LEGENDARY("LEGENDARY", "&6", ChatColor.GOLD, 4),
    MYTHIC("MYTHIC", "&d", ChatColor.LIGHT_PURPLE, 5),
    DIVINE("DIVINE", "&b", ChatColor.AQUA, 6),
    SPECIAL("SPECIAL", "&c", ChatColor.RED, 7),
    VERY_SPECIAL("VERY SPECIAL", "&c&l", ChatColor.RED, 8);

    private final String displayName;
    private final String colorCode;
    private final ChatColor chatColor;
    private final int tier;

    Rarity(String displayName, String colorCode, ChatColor chatColor, int tier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.chatColor = chatColor;
        this.tier = tier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public int getTier() {
        return tier;
    }

    /**
     * Get the formatted display (colored name).
     */
    public String getFormattedDisplay() {
        return colorCode + displayName;
    }

    /**
     * Get the next rarity tier.
     */
    public Rarity getNextRarity() {
        int nextTier = this.tier + 1;
        for (Rarity r : values()) {
            if (r.tier == nextTier) {
                return r;
            }
        }
        return this;
    }

    /**
     * Get the previous rarity tier.
     */
    public Rarity getPreviousRarity() {
        int prevTier = this.tier - 1;
        for (Rarity r : values()) {
            if (r.tier == prevTier) {
                return r;
            }
        }
        return this;
    }

    /**
     * Check if this rarity is higher than another.
     */
    public boolean isHigherThan(Rarity other) {
        return this.tier > other.tier;
    }

    /**
     * Check if this rarity is lower than another.
     */
    public boolean isLowerThan(Rarity other) {
        return this.tier < other.tier;
    }

    /**
     * Get rarity by tier.
     */
    public static Rarity getByTier(int tier) {
        for (Rarity r : values()) {
            if (r.tier == tier) {
                return r;
            }
        }
        return COMMON;
    }

    /**
     * Parse rarity from string.
     */
    public static Rarity fromString(String name) {
        if (name == null) return COMMON;

        String normalized = name.toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return Rarity.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Try partial match
            for (Rarity r : values()) {
                if (r.name().contains(normalized) || normalized.contains(r.name())) {
                    return r;
                }
            }
            return COMMON;
        }
    }

    /**
     * Get the base reforge cost for this rarity.
     */
    public int getReforgeCost() {
        switch (this) {
            case COMMON: return 250;
            case UNCOMMON: return 500;
            case RARE: return 1000;
            case EPIC: return 2500;
            case LEGENDARY: return 5000;
            case MYTHIC: return 10000;
            case DIVINE: return 25000;
            default: return 50000;
        }
    }
}
