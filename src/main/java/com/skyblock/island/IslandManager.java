package com.skyblock.island;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.skyblock.SkyblockPlugin;
import com.skyblock.api.events.IslandCreateEvent;
import com.skyblock.api.events.IslandTeleportEvent;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages all island operations.
 */
public class IslandManager implements Listener {

    private final SkyblockPlugin plugin;
    private final WorldManager worldManager;

    // Cache for islands by profile ID
    private final Cache<Integer, Island> islandCache;

    // Map of world name to island
    private final Map<String, Island> worldToIsland;

    // Players currently on islands (tracking for visitor XP)
    private final Map<UUID, IslandVisitSession> visitSessions;

    public IslandManager(SkyblockPlugin plugin, WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.worldToIsland = new HashMap<>();
        this.visitSessions = new HashMap<>();

        // Initialize cache
        int cacheSize = plugin.getConfigManager().getConfig().getInt("cache.island-data-size", 200);
        int cacheExpire = plugin.getConfigManager().getConfig().getInt("cache.island-data-expire", 30);

        this.islandCache = Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterAccess(cacheExpire, TimeUnit.MINUTES)
            .removalListener((key, value, cause) -> {
                if (value != null) {
                    saveIsland((Island) value);
                }
            })
            .build();
    }

    /**
     * Create a new island for a profile.
     */
    public CompletableFuture<Island> createIsland(int profileId, UUID ownerUuid) {
        // Generate island data
        UUID islandId = UUID.randomUUID();
        String worldName = "island_" + islandId.toString().substring(0, 8);

        Island island = new Island(islandId, profileId, worldName);
        island.addMember(ownerUuid, IslandRole.OWNER);
        island.setCreatedAt(System.currentTimeMillis());
        island.setLastAccessed(System.currentTimeMillis());

        // Fire event
        IslandCreateEvent event = new IslandCreateEvent(ownerUuid, island);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return CompletableFuture.completedFuture(null);
        }

        // Create the world
        return worldManager.createIslandWorld(worldName, ownerUuid)
            .thenApply(world -> {
                if (world == null) {
                    plugin.log(Level.SEVERE, "Failed to create island world for profile " + profileId);
                    return null;
                }

                // Set default spawn
                island.setSpawn(0, 100, 0, 0, 0);

                // Save to database
                saveIslandToDatabase(island);

                // Cache the island
                islandCache.put(profileId, island);
                worldToIsland.put(worldName, island);

                plugin.log(Level.INFO, "Created island for profile " + profileId + ": " + worldName);
                return island;
            });
    }

    /**
     * Get an island by profile ID.
     */
    public CompletableFuture<Island> getIsland(int profileId) {
        // Check cache first
        Island cached = islandCache.getIfPresent(profileId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Load from database
        return loadIslandFromDatabase(profileId).thenApply(island -> {
            if (island != null) {
                islandCache.put(profileId, island);
                worldToIsland.put(island.getWorldName(), island);
            }
            return island;
        });
    }

    /**
     * Get an island by world name.
     */
    public Island getIslandByWorld(String worldName) {
        return worldToIsland.get(worldName);
    }

    /**
     * Check if a profile has an island.
     */
    public CompletableFuture<Boolean> hasIsland(int profileId) {
        return getIsland(profileId).thenApply(island -> island != null);
    }

    /**
     * Teleport a player to their island.
     */
    public void teleportToIsland(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                // Create island if doesn't exist
                createIsland(profile.getId(), player.getUniqueId()).thenAccept(newIsland -> {
                    if (newIsland != null) {
                        doTeleportToIsland(player, newIsland);
                    } else {
                        player.sendMessage("§cFailed to create your island!");
                    }
                });
            } else {
                doTeleportToIsland(player, island);
            }
        });
    }

    /**
     * Teleport a player to visit another player's island.
     */
    public void visitIsland(Player visitor, Player islandOwner) {
        SkyblockPlayer ownerSb = plugin.getPlayerManager().getSkyblockPlayer(islandOwner);
        if (ownerSb == null) {
            visitor.sendMessage("§cThat player doesn't have an island!");
            return;
        }

        PlayerProfile ownerProfile = ownerSb.getActiveProfile();
        if (ownerProfile == null) {
            visitor.sendMessage("§cThat player doesn't have an active profile!");
            return;
        }

        getIsland(ownerProfile.getId()).thenAccept(island -> {
            if (island == null) {
                visitor.sendMessage("§cThat player doesn't have an island!");
                return;
            }

            // Check if island is public or visitor has permission
            if (!island.isPublic() && !island.isMember(visitor.getUniqueId())) {
                visitor.sendMessage("§cThat island is private!");
                return;
            }

            // Check if visitor is banned
            if (island.isBanned(visitor.getUniqueId())) {
                visitor.sendMessage("§cYou are banned from that island!");
                return;
            }

            // Check guest limit
            if (!island.canAcceptVisitors() && !island.isMember(visitor.getUniqueId())) {
                visitor.sendMessage("§cThat island has reached its visitor limit!");
                return;
            }

            doTeleportToIsland(visitor, island);
        });
    }

    /**
     * Internal teleport method.
     */
    private void doTeleportToIsland(Player player, Island island) {
        // Fire event
        IslandTeleportEvent event = new IslandTeleportEvent(player, island);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        island.updateLastAccess();

        worldManager.loadIslandWorld(island.getWorldName()).thenAccept(world -> {
            if (world == null) {
                player.sendMessage("§cFailed to load island!");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Location spawn = island.getSpawnLocation(world);
                player.teleport(spawn);

                // Track visit if not owner/member
                if (!island.isMember(player.getUniqueId())) {
                    island.addVisitor(player.getUniqueId());
                    startVisitSession(player.getUniqueId(), island);

                    // Award social XP to island owner
                    awardSocialXp(island.getOwner(), 5);
                }
            });
        });
    }

    /**
     * Set the island spawn point.
     */
    public void setIslandSpawn(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            if (!island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou can't set spawn on an island you don't own!");
                return;
            }

            Location loc = player.getLocation();
            island.setSpawn(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            saveIsland(island);

            player.sendMessage("§aIsland spawn point set!");
        });
    }

    /**
     * Reset an island (dangerous operation).
     */
    public CompletableFuture<Boolean> resetIsland(int profileId, UUID requesterUuid) {
        return getIsland(profileId).thenCompose(island -> {
            if (island == null) {
                return CompletableFuture.completedFuture(false);
            }

            // Only owner can reset
            if (!island.isOwner(requesterUuid)) {
                return CompletableFuture.completedFuture(false);
            }

            String worldName = island.getWorldName();

            // Delete the world
            worldManager.deleteWorld(worldName);

            // Remove from cache
            islandCache.invalidate(profileId);
            worldToIsland.remove(worldName);

            // Delete from database
            return deleteIslandFromDatabase(island.getId()).thenApply(success -> {
                if (success) {
                    plugin.log(Level.INFO, "Reset island for profile " + profileId);
                }
                return success;
            });
        });
    }

    /**
     * Save an island to the database.
     */
    public void saveIsland(Island island) {
        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            saveIslandToDatabase(island);
        });
    }

    private void saveIslandToDatabase(Island island) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            // Save main island data
            String sql = """
                INSERT OR REPLACE INTO islands
                (id, profile_id, world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch,
                 size, created_at, last_accessed, is_public, pvp_enabled, guest_limit)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, island.getId().toString());
                stmt.setInt(2, island.getProfileId());
                stmt.setString(3, island.getWorldName());
                stmt.setDouble(4, island.getSpawnX());
                stmt.setDouble(5, island.getSpawnY());
                stmt.setDouble(6, island.getSpawnZ());
                stmt.setFloat(7, island.getSpawnYaw());
                stmt.setFloat(8, island.getSpawnPitch());
                stmt.setInt(9, island.getSize());
                stmt.setLong(10, island.getCreatedAt());
                stmt.setLong(11, island.getLastAccessed());
                stmt.setBoolean(12, island.isPublic());
                stmt.setBoolean(13, island.isPvpEnabled());
                stmt.setInt(14, island.getGuestLimit());
                stmt.executeUpdate();
            }

            // Save members
            String deleteMembersSql = "DELETE FROM island_members WHERE island_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteMembersSql)) {
                stmt.setString(1, island.getId().toString());
                stmt.executeUpdate();
            }

            String insertMemberSql = "INSERT INTO island_members (island_id, player_uuid, role, joined_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertMemberSql)) {
                for (Map.Entry<UUID, IslandRole> entry : island.getMembers().entrySet()) {
                    stmt.setString(1, island.getId().toString());
                    stmt.setString(2, entry.getKey().toString());
                    stmt.setString(3, entry.getValue().name());
                    stmt.setLong(4, System.currentTimeMillis());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // Save settings
            String deleteSettingsSql = "DELETE FROM island_settings WHERE island_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSettingsSql)) {
                stmt.setString(1, island.getId().toString());
                stmt.executeUpdate();
            }

            String insertSettingSql = "INSERT INTO island_settings (island_id, setting_key, setting_value) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSettingSql)) {
                for (Map.Entry<String, String> entry : island.getSettings().entrySet()) {
                    stmt.setString(1, island.getId().toString());
                    stmt.setString(2, entry.getKey());
                    stmt.setString(3, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to save island: " + e.getMessage());
        }
    }

    private CompletableFuture<Island> loadIslandFromDatabase(int profileId) {
        return plugin.getDatabaseManager().executeAsync(conn -> {
            String sql = "SELECT * FROM islands WHERE profile_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, profileId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        String worldName = rs.getString("world_name");

                        Island island = new Island(id, profileId, worldName);
                        island.setSpawn(
                            rs.getDouble("spawn_x"),
                            rs.getDouble("spawn_y"),
                            rs.getDouble("spawn_z"),
                            rs.getFloat("spawn_yaw"),
                            rs.getFloat("spawn_pitch")
                        );
                        island.setSize(rs.getInt("size"));
                        island.setCreatedAt(rs.getLong("created_at"));
                        island.setLastAccessed(rs.getLong("last_accessed"));
                        island.setPublic(rs.getBoolean("is_public"));
                        island.setPvpEnabled(rs.getBoolean("pvp_enabled"));
                        island.setGuestLimit(rs.getInt("guest_limit"));

                        // Load members
                        loadIslandMembers(conn, island);

                        // Load settings
                        loadIslandSettings(conn, island);

                        // Load visitor history
                        loadVisitorHistory(conn, island);

                        // Load bans
                        loadIslandBans(conn, island);

                        return island;
                    }
                }
            }
            return null;
        });
    }

    private void loadIslandMembers(Connection conn, Island island) throws SQLException {
        String sql = "SELECT player_uuid, role FROM island_members WHERE island_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, island.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                    IslandRole role = IslandRole.fromString(rs.getString("role"));
                    island.addMember(uuid, role);
                }
            }
        }
    }

    private void loadIslandSettings(Connection conn, Island island) throws SQLException {
        String sql = "SELECT setting_key, setting_value FROM island_settings WHERE island_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, island.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    island.setSetting(rs.getString("setting_key"), rs.getString("setting_value"));
                }
            }
        }
    }

    private void loadVisitorHistory(Connection conn, Island island) throws SQLException {
        String sql = "SELECT visitor_uuid, visit_count, total_time_seconds, last_visit FROM island_visitors WHERE island_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, island.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("visitor_uuid"));
                    Island.VisitorData data = new Island.VisitorData();
                    data.setVisitCount(rs.getInt("visit_count"));
                    data.setTotalTimeSeconds(rs.getLong("total_time_seconds"));
                    data.setLastVisit(rs.getLong("last_visit"));
                    island.setVisitorHistory(uuid, data);
                }
            }
        }
    }

    private void loadIslandBans(Connection conn, Island island) throws SQLException {
        String sql = "SELECT banned_uuid FROM island_bans WHERE island_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, island.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    island.banPlayer(UUID.fromString(rs.getString("banned_uuid")));
                }
            }
        }
    }

    private CompletableFuture<Boolean> deleteIslandFromDatabase(UUID islandId) {
        return plugin.getDatabaseManager().executeAsync(conn -> {
            String sql = "DELETE FROM islands WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, islandId.toString());
                return stmt.executeUpdate() > 0;
            }
        });
    }

    /**
     * Start tracking a visit session for social XP.
     */
    private void startVisitSession(UUID visitorUuid, Island island) {
        visitSessions.put(visitorUuid, new IslandVisitSession(island, System.currentTimeMillis()));
    }

    /**
     * End a visit session and award XP.
     */
    private void endVisitSession(UUID visitorUuid) {
        IslandVisitSession session = visitSessions.remove(visitorUuid);
        if (session != null) {
            long duration = System.currentTimeMillis() - session.startTime;
            int minutes = (int) (duration / 60000);

            // Award 1 social XP per minute (capped at 30 per visit)
            int xp = Math.min(minutes, 30);
            if (xp > 0) {
                awardSocialXp(session.island.getOwner(), xp);
            }

            // Update visitor history
            Island.VisitorData data = session.island.getVisitorHistory().get(visitorUuid);
            if (data != null) {
                data.endSession();
            }
        }
    }

    /**
     * Award social skill XP to a player.
     */
    private void awardSocialXp(UUID playerUuid, int amount) {
        if (playerUuid == null) return;

        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && plugin.getModuleManager().isModuleEnabled("skills")) {
            plugin.getSkillManager().addXp(player, "social", amount);
        }
    }

    // Event handlers
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // End any active visit session
        endVisitSession(event.getPlayer().getUniqueId());

        // Remove from island visitor lists
        for (Island island : islandCache.asMap().values()) {
            island.removeVisitor(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();
        World to = player.getWorld();

        // End session if leaving an island
        Island fromIsland = getIslandByWorld(from.getName());
        if (fromIsland != null && !fromIsland.isMember(player.getUniqueId())) {
            fromIsland.removeVisitor(player.getUniqueId());
            endVisitSession(player.getUniqueId());
        }

        // Update world access time
        Island toIsland = getIslandByWorld(to.getName());
        if (toIsland != null) {
            worldManager.updateWorldAccess(to.getName());
        }
    }

    public void shutdown() {
        // Save all cached islands
        for (Island island : islandCache.asMap().values()) {
            saveIsland(island);
        }

        // End all visit sessions
        for (UUID visitor : new ArrayList<>(visitSessions.keySet())) {
            endVisitSession(visitor);
        }
    }

    /**
     * Track visit session.
     */
    private static class IslandVisitSession {
        final Island island;
        final long startTime;

        IslandVisitSession(Island island, long startTime) {
            this.island = island;
            this.startTime = startTime;
        }
    }
}
