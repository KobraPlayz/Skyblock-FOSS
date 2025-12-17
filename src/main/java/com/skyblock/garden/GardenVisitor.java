package com.skyblock.garden;

import java.util.UUID;

/**
 * Represents an NPC visitor in the garden.
 */
public class GardenVisitor {

    private final UUID id;
    private final String visitorType;
    private final String requestItem;
    private final int requestAmount;
    private final long rewardCopper;
    private final String rewardItemsJson;
    private final long spawnedAt;
    private final long expiresAt;
    private boolean completed;

    public GardenVisitor(String visitorType, String requestItem, int requestAmount,
                         long rewardCopper, String rewardItemsJson, long expiresAt) {
        this.id = UUID.randomUUID();
        this.visitorType = visitorType;
        this.requestItem = requestItem;
        this.requestAmount = requestAmount;
        this.rewardCopper = rewardCopper;
        this.rewardItemsJson = rewardItemsJson;
        this.spawnedAt = System.currentTimeMillis();
        this.expiresAt = expiresAt;
        this.completed = false;
    }

    public UUID getId() {
        return id;
    }

    public String getVisitorType() {
        return visitorType;
    }

    public String getRequestItem() {
        return requestItem;
    }

    public int getRequestAmount() {
        return requestAmount;
    }

    public long getRewardCopper() {
        return rewardCopper;
    }

    public String getRewardItemsJson() {
        return rewardItemsJson;
    }

    public long getSpawnedAt() {
        return spawnedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public long getTimeRemaining() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    /**
     * Get formatted time remaining.
     */
    public String getFormattedTimeRemaining() {
        long remaining = getTimeRemaining() / 1000;
        int minutes = (int) (remaining / 60);
        int seconds = (int) (remaining % 60);
        return minutes + "m " + seconds + "s";
    }

    /**
     * Visitor types with display names.
     */
    public enum VisitorType {
        FARMER("Farmer", "§a"),
        MERCHANT("Merchant", "§6"),
        ALCHEMIST("Alchemist", "§5"),
        CHEF("Chef", "§e"),
        BOTANIST("Botanist", "§2");

        private final String displayName;
        private final String color;

        VisitorType(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
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
    }
}
