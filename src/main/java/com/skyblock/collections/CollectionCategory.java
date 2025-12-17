package com.skyblock.collections;

/**
 * Represents a collection category.
 */
public class CollectionCategory {

    private final String id;
    private final String displayName;
    private final String icon;
    private final String description;

    public CollectionCategory(String id, String displayName, String icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }
}
