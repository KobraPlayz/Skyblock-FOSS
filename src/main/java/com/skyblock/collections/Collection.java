package com.skyblock.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a collection.
 */
public class Collection {

    private final String id;
    private final String displayName;
    private final String icon;
    private final CollectionCategory category;
    private final Map<Integer, CollectionTier> tiers;

    public Collection(String id, String displayName, String icon, CollectionCategory category) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.category = category;
        this.tiers = new LinkedHashMap<>();
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

    public CollectionCategory getCategory() {
        return category;
    }

    public Map<Integer, CollectionTier> getTiers() {
        return tiers;
    }

    public void addTier(int tier, CollectionTier tierData) {
        tiers.put(tier, tierData);
    }

    public CollectionTier getTier(int tier) {
        return tiers.get(tier);
    }

    public int getMaxTier() {
        return tiers.isEmpty() ? 0 : tiers.keySet().stream().max(Integer::compare).orElse(0);
    }
}
