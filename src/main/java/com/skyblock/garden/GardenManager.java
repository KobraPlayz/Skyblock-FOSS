package com.skyblock.garden;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.skyblock.SkyblockPlugin;
import com.skyblock.api.events.GardenUnlockEvent;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages garden operations.
 */
public class GardenManager implements Listener {

    private final SkyblockPlugin plugin;

    // Cache for gardens by profile ID
    private final Cache<Integer, Garden> gardenCache;

    // Configuration
    private final int unlockSkyblockLevel;
    private final int totalPlots;
    private final int plotSize;
    private final int visitorSpawnIntervalMinutes;
    private final int maxActiveVisitors;

    public GardenManager(SkyblockPlugin plugin) {
        this.plugin = plugin;

        // Load config
        unlockSkyblockLevel = plugin.getConfigManager().getGardenConfig()
            .getInt("garden.unlock_skyblock_level", 5);
        totalPlots = plugin.getConfigManager().getGardenConfig()
            .getInt("garden.total_plots", 24);
        plotSize = plugin.getConfigManager().getGardenConfig()
            .getInt("garden.plot_size", 96);
        visitorSpawnIntervalMinutes = plugin.getConfigManager().getGardenConfig()
            .getInt("visitors.spawn_interval_minutes", 10);
        maxActiveVisitors = plugin.getConfigManager().getGardenConfig()
            .getInt("visitors.max_active_visitors", 3);

        // Initialize cache
        int cacheSize = plugin.getConfigManager().getConfig().getInt("cache.garden-data-size", 100);
        int cacheExpire = plugin.getConfigManager().getConfig().getInt("cache.garden-data-expire", 30);

        this.gardenCache = Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterAccess(cacheExpire, TimeUnit.MINUTES)
            .removalListener((key, value, cause) -> {
                if (value != null) {
                    saveGarden((Garden) value);
                }
            })
            .build();

        // Start visitor spawn task
        startVisitorSpawnTask();
    }

    /**
     * Check if a player can unlock their garden.
     */
    public boolean canUnlockGarden(Player player) {
        // Check Skyblock level (based on skill average)
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return false;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return false;

        // Calculate Skyblock level from skill average
        int skillAverage = calculateSkillAverage(profile);
        return skillAverage >= unlockSkyblockLevel;
    }

    /**
     * Calculate skill average (simplified Skyblock level).
     */
    private int calculateSkillAverage(PlayerProfile profile) {
        int total = 0;
        int count = 0;

        for (PlayerProfile.SkillData data : profile.getSkills().values()) {
            total += data.getLevel();
            count++;
        }

        return count > 0 ? total / count : 0;
    }

    /**
     * Unlock garden for a profile.
     */
    public CompletableFuture<Garden> unlockGarden(Player player, int profileId) {
        if (!canUnlockGarden(player)) {
            player.sendMessage("§cYou need Skyblock Level " + unlockSkyblockLevel + " to unlock the Garden!");
            return CompletableFuture.completedFuture(null);
        }

        UUID gardenId = UUID.randomUUID();
        Garden garden = new Garden(gardenId, profileId);
        garden.setUnlockedAt(System.currentTimeMillis());

        // Fire event
        GardenUnlockEvent event = new GardenUnlockEvent(player, garden);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return CompletableFuture.completedFuture(null);
        }

        // Save to database
        saveGardenToDatabase(garden);

        // Cache
        gardenCache.put(profileId, garden);

        player.sendMessage("§a§lGarden Unlocked!");
        player.sendMessage("§7Use §e/garden §7to visit your garden.");

        return CompletableFuture.completedFuture(garden);
    }

    /**
     * Get garden by profile ID.
     */
    public CompletableFuture<Garden> getGarden(int profileId) {
        // Check cache
        Garden cached = gardenCache.getIfPresent(profileId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Load from database
        return loadGardenFromDatabase(profileId).thenApply(garden -> {
            if (garden != null) {
                gardenCache.put(profileId, garden);
            }
            return garden;
        });
    }

    /**
     * Check if profile has garden.
     */
    public CompletableFuture<Boolean> hasGarden(int profileId) {
        return getGarden(profileId).thenApply(garden -> garden != null);
    }

    /**
     * Teleport player to their garden.
     */
    public void teleportToGarden(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) {
            player.sendMessage("§cYou don't have an active profile!");
            return;
        }

        getGarden(profile.getId()).thenAccept(garden -> {
            if (garden == null) {
                // Try to unlock
                if (canUnlockGarden(player)) {
                    player.sendMessage("§eUnlocking your garden...");
                    unlockGarden(player, profile.getId()).thenAccept(newGarden -> {
                        if (newGarden != null) {
                            doTeleportToGarden(player, newGarden);
                        }
                    });
                } else {
                    player.sendMessage("§cYou need Skyblock Level " + unlockSkyblockLevel + " to unlock the Garden!");
                }
                return;
            }

            doTeleportToGarden(player, garden);
        });
    }

    private void doTeleportToGarden(Player player, Garden garden) {
        // For now, teleport to a location in the island world
        // In a full implementation, this would be a separate garden world

        plugin.getIslandManager().getIsland(garden.getProfileId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou need an island first!");
                return;
            }

            plugin.getWorldManager().loadIslandWorld(island.getWorldName()).thenAccept(world -> {
                if (world == null) {
                    player.sendMessage("§cFailed to load garden!");
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Garden is at a specific offset from island spawn
                    // This is simplified - full implementation would use separate world
                    Location gardenSpawn = new Location(world, 200, 100, 200);
                    player.teleport(gardenSpawn);
                    player.sendMessage("§aWelcome to your Garden!");
                });
            });
        });
    }

    /**
     * Handle crop harvesting for milestones.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getModuleManager().isModuleEnabled("garden")) return;

        Player player = event.getPlayer();
        CropType crop = CropType.fromBlock(event.getBlock().getType());
        if (crop == null) return;

        // Only count in garden world/area
        // Simplified: count all crops for now
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        getGarden(profile.getId()).thenAccept(garden -> {
            if (garden == null) return;

            // Check if player can farm this crop
            if (!garden.canFarmCrop(crop)) {
                return;
            }

            // Add to milestone
            garden.addCropHarvested(crop, 1);

            // Check for milestone rewards
            Garden.CropMilestone milestone = garden.getMilestone(crop);
            if (milestone != null) {
                // Award Garden XP based on milestone tier
                garden.addGardenXp(milestone.getCurrentTier() * 0.5);
            }

            // Apply farming fortune bonus
            int fortuneBonus = garden.getCropFarmingFortune(crop);
            if (fortuneBonus > 0) {
                // Could apply extra drops here
            }
        });
    }

    /**
     * Save garden to database.
     */
    public void saveGarden(Garden garden) {
        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            saveGardenToDatabase(garden);
        });
    }

    private void saveGardenToDatabase(Garden garden) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            // Save main garden data
            String sql = """
                INSERT OR REPLACE INTO gardens
                (id, profile_id, world_name, garden_level, garden_xp, copper_balance, compost_balance, unlocked_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, garden.getId().toString());
                stmt.setInt(2, garden.getProfileId());
                stmt.setString(3, garden.getWorldName());
                stmt.setInt(4, garden.getGardenLevel());
                stmt.setDouble(5, garden.getGardenXp());
                stmt.setLong(6, garden.getCopperBalance());
                stmt.setLong(7, garden.getCompostBalance());
                stmt.setLong(8, garden.getUnlockedAt());
                stmt.executeUpdate();
            }

            // Save plots
            String deletePlotsSql = "DELETE FROM garden_plots WHERE garden_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deletePlotsSql)) {
                stmt.setString(1, garden.getId().toString());
                stmt.executeUpdate();
            }

            String insertPlotSql = """
                INSERT INTO garden_plots (garden_id, plot_number, unlocked, cleaned, preset_type, crop_type)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement stmt = conn.prepareStatement(insertPlotSql)) {
                for (GardenPlot plot : garden.getPlots().values()) {
                    stmt.setString(1, garden.getId().toString());
                    stmt.setInt(2, plot.getPlotNumber());
                    stmt.setBoolean(3, plot.isUnlocked());
                    stmt.setBoolean(4, plot.isCleaned());
                    stmt.setString(5, plot.getPresetType());
                    stmt.setString(6, plot.getCropType() != null ? plot.getCropType().name() : null);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // Save crop upgrades
            String deleteUpgradesSql = "DELETE FROM garden_crop_upgrades WHERE garden_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteUpgradesSql)) {
                stmt.setString(1, garden.getId().toString());
                stmt.executeUpdate();
            }

            String insertUpgradeSql = "INSERT INTO garden_crop_upgrades (garden_id, crop_type, upgrade_level) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertUpgradeSql)) {
                for (var entry : garden.getCropUpgrades().entrySet()) {
                    if (entry.getValue() > 0) {
                        stmt.setString(1, garden.getId().toString());
                        stmt.setString(2, entry.getKey().name());
                        stmt.setInt(3, entry.getValue());
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            }

            // Save milestones
            String deleteMilestonesSql = "DELETE FROM garden_milestones WHERE garden_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteMilestonesSql)) {
                stmt.setString(1, garden.getId().toString());
                stmt.executeUpdate();
            }

            String insertMilestoneSql = "INSERT INTO garden_milestones (garden_id, crop_type, amount_farmed, milestone_tier) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertMilestoneSql)) {
                for (var entry : garden.getMilestones().entrySet()) {
                    Garden.CropMilestone ms = entry.getValue();
                    if (ms.getAmountFarmed() > 0) {
                        stmt.setString(1, garden.getId().toString());
                        stmt.setString(2, entry.getKey().name());
                        stmt.setLong(3, ms.getAmountFarmed());
                        stmt.setInt(4, ms.getCurrentTier());
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            }

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to save garden: " + e.getMessage());
        }
    }

    private CompletableFuture<Garden> loadGardenFromDatabase(int profileId) {
        return plugin.getDatabaseManager().executeAsync(conn -> {
            String sql = "SELECT * FROM gardens WHERE profile_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, profileId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        Garden garden = new Garden(id, profileId);
                        garden.setWorldName(rs.getString("world_name"));
                        garden.setGardenLevel(rs.getInt("garden_level"));
                        garden.setGardenXp(rs.getDouble("garden_xp"));
                        garden.setCopperBalance(rs.getLong("copper_balance"));
                        garden.setCompostBalance(rs.getLong("compost_balance"));
                        garden.setUnlockedAt(rs.getLong("unlocked_at"));

                        // Load plots
                        loadGardenPlots(conn, garden);

                        // Load crop upgrades
                        loadCropUpgrades(conn, garden);

                        // Load milestones
                        loadMilestones(conn, garden);

                        return garden;
                    }
                }
            }
            return null;
        });
    }

    private void loadGardenPlots(Connection conn, Garden garden) throws SQLException {
        String sql = "SELECT * FROM garden_plots WHERE garden_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, garden.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int plotNumber = rs.getInt("plot_number");
                    GardenPlot plot = garden.getPlot(plotNumber);
                    if (plot != null) {
                        plot.setUnlocked(rs.getBoolean("unlocked"));
                        plot.setCleaned(rs.getBoolean("cleaned"));
                        plot.setPresetType(rs.getString("preset_type"));
                        String cropTypeStr = rs.getString("crop_type");
                        if (cropTypeStr != null) {
                            try {
                                plot.setCropType(CropType.valueOf(cropTypeStr));
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                }
            }
        }
    }

    private void loadCropUpgrades(Connection conn, Garden garden) throws SQLException {
        String sql = "SELECT crop_type, upgrade_level FROM garden_crop_upgrades WHERE garden_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, garden.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        CropType crop = CropType.valueOf(rs.getString("crop_type"));
                        int level = rs.getInt("upgrade_level");
                        // Set upgrade level (need to add setter method)
                        for (int i = 0; i < level; i++) {
                            garden.upgradeCrop(crop, 0); // Free upgrade during load
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    private void loadMilestones(Connection conn, Garden garden) throws SQLException {
        String sql = "SELECT crop_type, amount_farmed, milestone_tier FROM garden_milestones WHERE garden_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, garden.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        CropType crop = CropType.valueOf(rs.getString("crop_type"));
                        Garden.CropMilestone milestone = garden.getMilestone(crop);
                        if (milestone != null) {
                            milestone.setAmountFarmed(rs.getLong("amount_farmed"));
                            milestone.setCurrentTier(rs.getInt("milestone_tier"));
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    /**
     * Start visitor spawn task.
     */
    private void startVisitorSpawnTask() {
        long intervalTicks = visitorSpawnIntervalMinutes * 60 * 20L;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Spawn visitors for online players with gardens
            for (Player player : Bukkit.getOnlinePlayers()) {
                SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
                if (sbPlayer == null) continue;

                PlayerProfile profile = sbPlayer.getActiveProfile();
                if (profile == null) continue;

                Garden garden = gardenCache.getIfPresent(profile.getId());
                if (garden == null) continue;

                // Clear expired visitors
                garden.clearExpiredVisitors();

                // Maybe spawn a new visitor
                if (garden.getActiveVisitors().size() < maxActiveVisitors) {
                    if (Math.random() < 0.3) { // 30% chance
                        spawnVisitor(garden);
                    }
                }
            }
        }, intervalTicks, intervalTicks);
    }

    /**
     * Spawn a random visitor in a garden.
     */
    private void spawnVisitor(Garden garden) {
        // Random visitor type
        GardenVisitor.VisitorType[] types = GardenVisitor.VisitorType.values();
        GardenVisitor.VisitorType type = types[(int) (Math.random() * types.length)];

        // Random crop request
        CropType[] crops = CropType.values();
        CropType requestCrop = crops[(int) (Math.random() * crops.length)];

        // Random amount
        int amount = 16 + (int) (Math.random() * 48); // 16-64

        // Reward
        long copperReward = amount * 2L;

        // Expire in 30 minutes
        long expiresAt = System.currentTimeMillis() + (30 * 60 * 1000L);

        GardenVisitor visitor = new GardenVisitor(
            type.name(),
            requestCrop.name(),
            amount,
            copperReward,
            null,
            expiresAt
        );

        garden.addVisitor(visitor);
    }

    public void shutdown() {
        // Save all cached gardens
        for (Garden garden : gardenCache.asMap().values()) {
            saveGarden(garden);
        }
    }
}
