package com.skyblock.items.stats;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents the stats on an item.
 */
public class ItemStats {

    private final Map<StatType, Double> stats;

    public ItemStats() {
        this.stats = new EnumMap<>(StatType.class);
    }

    public ItemStats(Map<StatType, Double> stats) {
        this.stats = new EnumMap<>(StatType.class);
        this.stats.putAll(stats);
    }

    /**
     * Get a stat value.
     */
    public double getStat(StatType type) {
        return stats.getOrDefault(type, 0.0);
    }

    /**
     * Set a stat value.
     */
    public void setStat(StatType type, double value) {
        if (value == 0) {
            stats.remove(type);
        } else {
            stats.put(type, value);
        }
    }

    /**
     * Add to a stat value.
     */
    public void addStat(StatType type, double value) {
        double current = getStat(type);
        setStat(type, current + value);
    }

    /**
     * Check if has a stat.
     */
    public boolean hasStat(StatType type) {
        return stats.containsKey(type) && stats.get(type) != 0;
    }

    /**
     * Get all stats.
     */
    public Map<StatType, Double> getStats() {
        return new EnumMap<>(stats);
    }

    /**
     * Check if has any stats.
     */
    public boolean isEmpty() {
        return stats.isEmpty();
    }

    /**
     * Combine stats from another ItemStats object.
     */
    public ItemStats combine(ItemStats other) {
        ItemStats combined = new ItemStats(this.stats);
        for (Map.Entry<StatType, Double> entry : other.stats.entrySet()) {
            combined.addStat(entry.getKey(), entry.getValue());
        }
        return combined;
    }

    /**
     * Multiply all stats by a value.
     */
    public ItemStats multiply(double multiplier) {
        ItemStats result = new ItemStats();
        for (Map.Entry<StatType, Double> entry : stats.entrySet()) {
            result.setStat(entry.getKey(), entry.getValue() * multiplier);
        }
        return result;
    }

    /**
     * Create a copy of these stats.
     */
    public ItemStats copy() {
        return new ItemStats(this.stats);
    }

    /**
     * Parse stats from a configuration map.
     */
    public static ItemStats fromMap(Map<String, Object> map) {
        ItemStats stats = new ItemStats();
        if (map == null) return stats;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            StatType type = StatType.fromString(entry.getKey());
            if (type != null && entry.getValue() instanceof Number) {
                stats.setStat(type, ((Number) entry.getValue()).doubleValue());
            }
        }

        return stats;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ItemStats{");
        for (Map.Entry<StatType, Double> entry : stats.entrySet()) {
            sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(", ");
        }
        if (!stats.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }
}
