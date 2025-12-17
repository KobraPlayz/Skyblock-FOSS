package com.skyblock.island;

/**
 * Roles for island members.
 */
public enum IslandRole {
    OWNER("Owner", "§6", 100),
    MEMBER("Member", "§a", 50);

    private final String displayName;
    private final String color;
    private final int priority;

    IslandRole(String displayName, String color, int priority) {
        this.displayName = displayName;
        this.color = color;
        this.priority = priority;
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

    public int getPriority() {
        return priority;
    }

    public boolean hasHigherOrEqualPriority(IslandRole other) {
        return this.priority >= other.priority;
    }

    public static IslandRole fromString(String name) {
        for (IslandRole role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return MEMBER;
    }
}
