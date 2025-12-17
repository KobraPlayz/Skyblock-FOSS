package com.skyblock.island;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

/**
 * Represents a player's private island.
 */
public class Island {

    private final UUID id;
    private final int profileId;
    private final String worldName;

    // Spawn point
    private double spawnX;
    private double spawnY;
    private double spawnZ;
    private float spawnYaw;
    private float spawnPitch;

    // Island properties
    private int size;
    private long createdAt;
    private long lastAccessed;
    private boolean isPublic;
    private boolean pvpEnabled;
    private int guestLimit;

    // Members (for co-op)
    private final Map<UUID, IslandRole> members;

    // Settings
    private final Map<String, String> settings;

    // Visitors
    private final Set<UUID> currentVisitors;
    private final Map<UUID, VisitorData> visitorHistory;

    // Bans
    private final Set<UUID> bannedPlayers;

    public Island(UUID id, int profileId, String worldName) {
        this.id = id;
        this.profileId = profileId;
        this.worldName = worldName;
        this.spawnX = 0;
        this.spawnY = 100;
        this.spawnZ = 0;
        this.spawnYaw = 0;
        this.spawnPitch = 0;
        this.size = 160;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessed = System.currentTimeMillis();
        this.isPublic = false;
        this.pvpEnabled = false;
        this.guestLimit = 5;
        this.members = new HashMap<>();
        this.settings = new HashMap<>();
        this.currentVisitors = new HashSet<>();
        this.visitorHistory = new HashMap<>();
        this.bannedPlayers = new HashSet<>();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public int getProfileId() {
        return profileId;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getSpawnX() {
        return spawnX;
    }

    public double getSpawnY() {
        return spawnY;
    }

    public double getSpawnZ() {
        return spawnZ;
    }

    public float getSpawnYaw() {
        return spawnYaw;
    }

    public float getSpawnPitch() {
        return spawnPitch;
    }

    public int getSize() {
        return size;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public int getGuestLimit() {
        return guestLimit;
    }

    // Setters
    public void setSpawn(double x, double y, double z, float yaw, float pitch) {
        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;
        this.spawnYaw = yaw;
        this.spawnPitch = pitch;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public void setGuestLimit(int guestLimit) {
        this.guestLimit = guestLimit;
    }

    /**
     * Get the spawn location for this island.
     */
    public Location getSpawnLocation(World world) {
        return new Location(world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
    }

    /**
     * Update last access time.
     */
    public void updateLastAccess() {
        this.lastAccessed = System.currentTimeMillis();
    }

    // Member management
    public Map<UUID, IslandRole> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    public void addMember(UUID playerUuid, IslandRole role) {
        members.put(playerUuid, role);
    }

    public void removeMember(UUID playerUuid) {
        members.remove(playerUuid);
    }

    public IslandRole getMemberRole(UUID playerUuid) {
        return members.get(playerUuid);
    }

    public boolean isMember(UUID playerUuid) {
        return members.containsKey(playerUuid);
    }

    public boolean isOwner(UUID playerUuid) {
        return members.get(playerUuid) == IslandRole.OWNER;
    }

    public UUID getOwner() {
        for (Map.Entry<UUID, IslandRole> entry : members.entrySet()) {
            if (entry.getValue() == IslandRole.OWNER) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getMemberCount() {
        return members.size();
    }

    public boolean isCoop() {
        return members.size() > 1;
    }

    // Settings management
    public Map<String, String> getSettings() {
        return Collections.unmodifiableMap(settings);
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public String getSetting(String key) {
        return settings.get(key);
    }

    public String getSetting(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = settings.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // Visitor management
    public Set<UUID> getCurrentVisitors() {
        return Collections.unmodifiableSet(currentVisitors);
    }

    public void addVisitor(UUID visitorUuid) {
        currentVisitors.add(visitorUuid);

        // Update visitor history
        VisitorData data = visitorHistory.computeIfAbsent(visitorUuid, k -> new VisitorData());
        data.incrementVisit();
    }

    public void removeVisitor(UUID visitorUuid) {
        currentVisitors.remove(visitorUuid);
    }

    public boolean isVisitor(UUID playerUuid) {
        return currentVisitors.contains(playerUuid);
    }

    public int getVisitorCount() {
        return currentVisitors.size();
    }

    public boolean canAcceptVisitors() {
        return currentVisitors.size() < guestLimit;
    }

    public Map<UUID, VisitorData> getVisitorHistory() {
        return Collections.unmodifiableMap(visitorHistory);
    }

    public void setVisitorHistory(UUID uuid, VisitorData data) {
        visitorHistory.put(uuid, data);
    }

    // Ban management
    public Set<UUID> getBannedPlayers() {
        return Collections.unmodifiableSet(bannedPlayers);
    }

    public void banPlayer(UUID playerUuid) {
        bannedPlayers.add(playerUuid);
    }

    public void unbanPlayer(UUID playerUuid) {
        bannedPlayers.remove(playerUuid);
    }

    public boolean isBanned(UUID playerUuid) {
        return bannedPlayers.contains(playerUuid);
    }

    /**
     * Check if a location is within the island boundaries.
     */
    public boolean isWithinBounds(Location location) {
        if (location == null) return false;

        int halfSize = size / 2;
        double centerX = 0; // Islands are centered at 0,0
        double centerZ = 0;

        return location.getX() >= centerX - halfSize &&
               location.getX() <= centerX + halfSize &&
               location.getZ() >= centerZ - halfSize &&
               location.getZ() <= centerZ + halfSize;
    }

    /**
     * Check if a player can build on this island.
     */
    public boolean canBuild(UUID playerUuid) {
        return isMember(playerUuid);
    }

    /**
     * Check if a player can interact with blocks on this island.
     */
    public boolean canInteract(UUID playerUuid) {
        // Members can always interact
        if (isMember(playerUuid)) return true;

        // Check visitor permissions
        return getBooleanSetting("allow_visitor_interact", true);
    }

    /**
     * Visitor data tracking.
     */
    public static class VisitorData {
        private int visitCount;
        private long totalTimeSeconds;
        private long lastVisit;
        private long currentSessionStart;

        public VisitorData() {
            this.visitCount = 0;
            this.totalTimeSeconds = 0;
            this.lastVisit = System.currentTimeMillis();
        }

        public void incrementVisit() {
            visitCount++;
            lastVisit = System.currentTimeMillis();
            currentSessionStart = System.currentTimeMillis();
        }

        public void endSession() {
            if (currentSessionStart > 0) {
                totalTimeSeconds += (System.currentTimeMillis() - currentSessionStart) / 1000;
                currentSessionStart = 0;
            }
        }

        public int getVisitCount() {
            return visitCount;
        }

        public void setVisitCount(int visitCount) {
            this.visitCount = visitCount;
        }

        public long getTotalTimeSeconds() {
            return totalTimeSeconds;
        }

        public void setTotalTimeSeconds(long totalTimeSeconds) {
            this.totalTimeSeconds = totalTimeSeconds;
        }

        public long getLastVisit() {
            return lastVisit;
        }

        public void setLastVisit(long lastVisit) {
            this.lastVisit = lastVisit;
        }
    }
}
