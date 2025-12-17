package com.skyblock.items.stats;

/**
 * Enum representing all stat types in the game.
 */
public enum StatType {

    // Combat stats
    DAMAGE("Damage", "❁", "&c", ""),
    STRENGTH("Strength", "❁", "&c", ""),
    CRIT_CHANCE("Crit Chance", "☣", "&9", "%"),
    CRIT_DAMAGE("Crit Damage", "☠", "&9", "%"),
    ATTACK_SPEED("Bonus Attack Speed", "⚔", "&e", "%"),
    FEROCITY("Ferocity", "⫽", "&c", ""),
    ABILITY_DAMAGE("Ability Damage", "๑", "&c", "%"),

    // Defense stats
    HEALTH("Health", "❤", "&c", ""),
    DEFENSE("Defense", "❈", "&a", ""),
    TRUE_DEFENSE("True Defense", "❂", "&f", ""),

    // Utility stats
    SPEED("Speed", "✦", "&f", ""),
    INTELLIGENCE("Intelligence", "✎", "&b", ""),
    MAGIC_FIND("Magic Find", "✯", "&b", ""),
    PET_LUCK("Pet Luck", "♣", "&d", ""),
    SEA_CREATURE_CHANCE("Sea Creature Chance", "α", "&3", "%"),

    // Gathering stats
    MINING_SPEED("Mining Speed", "⸕", "&6", ""),
    MINING_FORTUNE("Mining Fortune", "☘", "&6", ""),
    FARMING_FORTUNE("Farming Fortune", "☘", "&6", ""),
    FORAGING_FORTUNE("Foraging Fortune", "☘", "&6", ""),
    FISHING_SPEED("Fishing Speed", "☂", "&b", "");

    private final String displayName;
    private final String symbol;
    private final String color;
    private final String suffix;

    StatType(String displayName, String symbol, String color, String suffix) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
        this.suffix = suffix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getColor() {
        return color;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Format a stat value for display.
     */
    public String format(double value) {
        String sign = value >= 0 ? "+" : "";
        String valueStr;

        if (value == (int) value) {
            valueStr = String.valueOf((int) value);
        } else {
            valueStr = String.format("%.1f", value);
        }

        return color + symbol + " " + sign + valueStr + suffix + " " + displayName;
    }

    /**
     * Format a stat value for lore (shorter version).
     */
    public String formatShort(double value) {
        String sign = value >= 0 ? "+" : "";
        String valueStr;

        if (value == (int) value) {
            valueStr = String.valueOf((int) value);
        } else {
            valueStr = String.format("%.1f", value);
        }

        return color + sign + valueStr + suffix;
    }

    /**
     * Get the full display string for a stat.
     */
    public String getDisplayString() {
        return color + symbol + " " + displayName;
    }

    /**
     * Parse a stat type from string.
     */
    public static StatType fromString(String name) {
        try {
            return StatType.valueOf(name.toUpperCase().replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
