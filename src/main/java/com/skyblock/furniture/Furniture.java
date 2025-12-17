package com.skyblock.furniture;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Represents a placed furniture item.
 */
public class Furniture {

    private final UUID id;
    private final UUID islandId;
    private final FurnitureType type;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final UUID placedBy;
    private final long placedAt;
    private String dataJson;

    public Furniture(UUID islandId, FurnitureType type, Location location, UUID placedBy) {
        this.id = UUID.randomUUID();
        this.islandId = islandId;
        this.type = type;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.placedBy = placedBy;
        this.placedAt = System.currentTimeMillis();
    }

    public Furniture(UUID id, UUID islandId, FurnitureType type, double x, double y, double z,
                     float yaw, UUID placedBy, long placedAt) {
        this.id = id;
        this.islandId = islandId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.placedBy = placedBy;
        this.placedAt = placedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIslandId() {
        return islandId;
    }

    public FurnitureType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public UUID getPlacedBy() {
        return placedBy;
    }

    public long getPlacedAt() {
        return placedAt;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    /**
     * Get location in a specific world.
     */
    public Location getLocation(org.bukkit.World world) {
        return new Location(world, x, y, z, yaw, 0);
    }
}
