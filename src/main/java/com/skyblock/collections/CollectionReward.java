package com.skyblock.collections;

/**
 * Represents a reward for unlocking a collection tier.
 */
public class CollectionReward {

    private final String type; // "recipe", "trade", "unlock", "stat"
    private final String item;
    private final String description;

    public CollectionReward(String type, String item, String description) {
        this.type = type;
        this.item = item;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getItem() {
        return item;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this is a recipe unlock.
     */
    public boolean isRecipe() {
        return "recipe".equalsIgnoreCase(type);
    }

    /**
     * Check if this is a trade unlock.
     */
    public boolean isTrade() {
        return "trade".equalsIgnoreCase(type);
    }

    /**
     * Check if this is a general unlock.
     */
    public boolean isUnlock() {
        return "unlock".equalsIgnoreCase(type);
    }
}
