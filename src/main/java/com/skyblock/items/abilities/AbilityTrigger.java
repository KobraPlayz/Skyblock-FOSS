package com.skyblock.items.abilities;

/**
 * Enum representing how an item ability is triggered.
 */
public enum AbilityTrigger {

    RIGHT_CLICK("RIGHT CLICK", "&e&lRIGHT CLICK"),
    LEFT_CLICK("LEFT CLICK", "&e&lLEFT CLICK"),
    SNEAK("SNEAK", "&e&lSNEAK"),
    SNEAK_RIGHT_CLICK("SNEAK + RIGHT CLICK", "&e&lSNEAK RIGHT CLICK"),
    SNEAK_LEFT_CLICK("SNEAK + LEFT CLICK", "&e&lSNEAK LEFT CLICK"),
    DOUBLE_RIGHT_CLICK("DOUBLE RIGHT CLICK", "&e&lDOUBLE RIGHT CLICK"),
    PASSIVE("Passive", "&7Passive"),
    FULL_SET("Full Set Bonus", "&6Full Set Bonus"),
    USE("Use", "&e&lUSE");

    private final String name;
    private final String displayName;

    AbilityTrigger(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse a trigger from string.
     */
    public static AbilityTrigger fromString(String name) {
        if (name == null) return RIGHT_CLICK;

        String normalized = name.toUpperCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("+", "_");

        try {
            return AbilityTrigger.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Try partial match
            for (AbilityTrigger trigger : values()) {
                if (trigger.name().contains(normalized) ||
                    trigger.getName().toUpperCase().contains(normalized.replace("_", " "))) {
                    return trigger;
                }
            }
            return RIGHT_CLICK;
        }
    }

    /**
     * Check if this trigger is passive.
     */
    public boolean isPassive() {
        return this == PASSIVE || this == FULL_SET;
    }

    /**
     * Check if this trigger requires a click action.
     */
    public boolean isClickAction() {
        return this == RIGHT_CLICK || this == LEFT_CLICK ||
               this == SNEAK_RIGHT_CLICK || this == SNEAK_LEFT_CLICK ||
               this == DOUBLE_RIGHT_CLICK;
    }
}
