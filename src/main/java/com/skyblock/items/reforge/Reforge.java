package com.skyblock.items.reforge;

import com.skyblock.items.rarity.Rarity;
import com.skyblock.items.stats.ItemStats;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a reforge that can be applied to items.
 */
public class Reforge {

    private final String id;
    private final String displayName;
    private final String applicableTo; // "weapon", "armor", "tool", "accessory"
    private final Map<Rarity, ItemStats> statsByRarity;

    public Reforge(String id, String displayName, String applicableTo) {
        this.id = id;
        this.displayName = displayName;
        this.applicableTo = applicableTo;
        this.statsByRarity = new EnumMap<>(Rarity.class);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApplicableTo() {
        return applicableTo;
    }

    /**
     * Set stats for a specific rarity.
     */
    public void setStats(Rarity rarity, ItemStats stats) {
        statsByRarity.put(rarity, stats);
    }

    /**
     * Get stats for a specific rarity.
     */
    public ItemStats getStats(Rarity rarity) {
        return statsByRarity.getOrDefault(rarity, new ItemStats());
    }

    /**
     * Check if this reforge has stats for a rarity.
     */
    public boolean hasStats(Rarity rarity) {
        return statsByRarity.containsKey(rarity);
    }

    /**
     * Check if this reforge can be applied to a specific item type.
     */
    public boolean canApplyTo(String itemType) {
        return applicableTo.equalsIgnoreCase(itemType);
    }

    /**
     * Get all rarities this reforge has stats for.
     */
    public Map<Rarity, ItemStats> getAllStats() {
        return new EnumMap<>(statsByRarity);
    }
}
