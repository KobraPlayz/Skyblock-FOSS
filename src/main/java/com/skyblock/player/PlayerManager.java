package com.skyblock.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.skyblock.SkyblockPlugin;
import com.skyblock.api.events.ProfileSwitchEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
 * Manages player data loading, saving, and caching.
 */
public class PlayerManager implements Listener {

    private final SkyblockPlugin plugin;
    private final Cache<UUID, SkyblockPlayer> playerCache;
    private final Map<UUID, Long> profileSwitchCooldowns;

    public PlayerManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.profileSwitchCooldowns = new HashMap<>();

        // Initialize cache
        int cacheSize = plugin.getConfigManager().getConfig().getInt("cache.player-cache-size", 1000);
        int expireMinutes = plugin.getConfigManager().getConfig().getInt("cache.player-cache-expire", 30);

        this.playerCache = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(expireMinutes, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Load a player's data from the database.
     */
    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();

        plugin.getDatabaseManager().executeAsync(conn -> {
            // Check if player exists
            SkyblockPlayer skyblockPlayer = loadOrCreatePlayer(conn, uuid, username);
            playerCache.put(uuid, skyblockPlayer);
            return skyblockPlayer;
        }).thenAccept(sbPlayer -> {
            // Run on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.debug("Loaded player data for " + username);
            });
        }).exceptionally(ex -> {
            plugin.log(Level.SEVERE, "Failed to load player " + username + ": " + ex.getMessage());
            return null;
        });
    }

    /**
     * Load or create a player in the database.
     */
    private SkyblockPlayer loadOrCreatePlayer(Connection conn, UUID uuid, String username) throws SQLException {
        // Check if player exists
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            long now = System.currentTimeMillis();

            if (!rs.next()) {
                // Create new player
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO players (uuid, username, first_join, last_seen) VALUES (?, ?, ?, ?)")) {
                    insertStmt.setString(1, uuid.toString());
                    insertStmt.setString(2, username);
                    insertStmt.setLong(3, now);
                    insertStmt.setLong(4, now);
                    insertStmt.executeUpdate();
                }

                // Create default profile
                String defaultProfileName = plugin.getConfigManager().getConfig()
                        .getString("general.default-profile-name", "Apple");
                createProfile(conn, uuid, defaultProfileName, true);

                return new SkyblockPlayer(plugin, uuid, username, now);
            } else {
                // Update last seen
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE players SET last_seen = ?, username = ? WHERE uuid = ?")) {
                    updateStmt.setLong(1, now);
                    updateStmt.setString(2, username);
                    updateStmt.setString(3, uuid.toString());
                    updateStmt.executeUpdate();
                }

                long firstJoin = rs.getLong("first_join");
                SkyblockPlayer player = new SkyblockPlayer(plugin, uuid, username, firstJoin);

                // Load active profile
                loadActiveProfile(conn, player);

                return player;
            }
        }
    }

    /**
     * Create a new profile for a player.
     */
    private PlayerProfile createProfile(Connection conn, UUID playerUuid, String name, boolean setActive) throws SQLException {
        long now = System.currentTimeMillis();
        double startingCoins = plugin.getConfigManager().getConfig().getDouble("economy.starting-coins", 0);

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO profiles (player_uuid, profile_name, created_at, is_active, purse) VALUES (?, ?, ?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, name);
            stmt.setLong(3, now);
            stmt.setBoolean(4, setActive);
            stmt.setDouble(5, startingCoins);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int profileId = keys.getInt(1);
                PlayerProfile profile = new PlayerProfile(profileId, playerUuid, name, now);
                profile.setPurse(startingCoins);

                // Initialize skills for this profile
                initializeSkills(conn, profileId);

                return profile;
            }
        }
        return null;
    }

    /**
     * Initialize skills for a new profile.
     */
    private void initializeSkills(Connection conn, int profileId) throws SQLException {
        String[] skills = {"mining", "farming", "combat", "foraging", "fishing",
                "enchanting", "alchemy", "runecrafting", "social", "carpentry", "taming"};

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO skills (profile_id, skill_type, xp, level) VALUES (?, ?, 0, 0)")) {
            for (String skill : skills) {
                stmt.setInt(1, profileId);
                stmt.setString(2, skill);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Load the active profile for a player.
     */
    private void loadActiveProfile(Connection conn, SkyblockPlayer player) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM profiles WHERE player_uuid = ? AND is_active = TRUE")) {
            stmt.setString(1, player.getUuid().toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PlayerProfile profile = profileFromResultSet(rs);
                loadProfileData(conn, profile);
                player.setActiveProfile(profile);
            } else {
                // No active profile, try to find any profile
                try (PreparedStatement anyStmt = conn.prepareStatement(
                        "SELECT * FROM profiles WHERE player_uuid = ? LIMIT 1")) {
                    anyStmt.setString(1, player.getUuid().toString());
                    ResultSet anyRs = anyStmt.executeQuery();

                    if (anyRs.next()) {
                        PlayerProfile profile = profileFromResultSet(anyRs);
                        loadProfileData(conn, profile);
                        player.setActiveProfile(profile);

                        // Set as active
                        setProfileActive(conn, player.getUuid(), profile.getId());
                    }
                }
            }
        }
    }

    /**
     * Load all data for a profile (skills, collections).
     */
    private void loadProfileData(Connection conn, PlayerProfile profile) throws SQLException {
        // Load skills
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM skills WHERE profile_id = ?")) {
            stmt.setInt(1, profile.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String skillType = rs.getString("skill_type");
                double xp = rs.getDouble("xp");
                int level = rs.getInt("level");
                profile.setSkillData(skillType, xp, level);
            }
        }

        // Load collections
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM collections WHERE profile_id = ?")) {
            stmt.setInt(1, profile.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemType = rs.getString("item_type");
                long amount = rs.getLong("amount");
                int tier = rs.getInt("highest_tier");
                profile.setCollectionData(itemType, amount, tier);
            }
        }
    }

    /**
     * Create a PlayerProfile from a ResultSet.
     */
    private PlayerProfile profileFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
        String name = rs.getString("profile_name");
        long createdAt = rs.getLong("created_at");
        double purse = rs.getDouble("purse");
        double bank = rs.getDouble("bank_balance");

        PlayerProfile profile = new PlayerProfile(id, playerUuid, name, createdAt);
        profile.setPurse(purse);
        profile.setBankBalance(bank);
        return profile;
    }

    /**
     * Set a profile as active.
     */
    private void setProfileActive(Connection conn, UUID playerUuid, int profileId) throws SQLException {
        // Deactivate all profiles
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE profiles SET is_active = FALSE WHERE player_uuid = ?")) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        }

        // Activate the selected profile
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE profiles SET is_active = TRUE WHERE id = ?")) {
            stmt.setInt(1, profileId);
            stmt.executeUpdate();
        }
    }

    /**
     * Save a player's data to the database.
     */
    public void savePlayer(UUID uuid) {
        SkyblockPlayer player = playerCache.getIfPresent(uuid);
        if (player == null) return;

        PlayerProfile profile = player.getActiveProfile();
        if (profile == null) return;

        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            // Save profile
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE profiles SET purse = ?, bank_balance = ? WHERE id = ?")) {
                stmt.setDouble(1, profile.getPurse());
                stmt.setDouble(2, profile.getBankBalance());
                stmt.setInt(3, profile.getId());
                stmt.executeUpdate();
            }

            // Save skills
            for (Map.Entry<String, PlayerProfile.SkillData> entry : profile.getSkills().entrySet()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE skills SET xp = ?, level = ? WHERE profile_id = ? AND skill_type = ?")) {
                    stmt.setDouble(1, entry.getValue().getXp());
                    stmt.setInt(2, entry.getValue().getLevel());
                    stmt.setInt(3, profile.getId());
                    stmt.setString(4, entry.getKey());
                    stmt.executeUpdate();
                }
            }

            // Save collections
            for (Map.Entry<String, PlayerProfile.CollectionData> entry : profile.getCollections().entrySet()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO collections (profile_id, item_type, amount, highest_tier) VALUES (?, ?, ?, ?)")) {
                    stmt.setInt(1, profile.getId());
                    stmt.setString(2, entry.getKey());
                    stmt.setLong(3, entry.getValue().getAmount());
                    stmt.setInt(4, entry.getValue().getTier());
                    stmt.executeUpdate();
                }
            }
        }).exceptionally(ex -> {
            plugin.log(Level.SEVERE, "Failed to save player " + uuid + ": " + ex.getMessage());
            return null;
        });
    }

    /**
     * Get a SkyblockPlayer from cache.
     */
    public SkyblockPlayer getPlayer(UUID uuid) {
        return playerCache.getIfPresent(uuid);
    }

    /**
     * Get a SkyblockPlayer from cache by Player.
     */
    public SkyblockPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    /**
     * Get all profiles for a player.
     */
    public CompletableFuture<List<PlayerProfile>> getProfiles(UUID playerUuid) {
        return plugin.getDatabaseManager().executeAsync(conn -> {
            List<PlayerProfile> profiles = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM profiles WHERE player_uuid = ?")) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    profiles.add(profileFromResultSet(rs));
                }
            }

            return profiles;
        });
    }

    /**
     * Create a new profile for a player.
     */
    public CompletableFuture<PlayerProfile> createNewProfile(UUID playerUuid, String name) {
        return plugin.getDatabaseManager().executeAsync(conn -> {
            // Check max profiles
            int maxProfiles = plugin.getConfigManager().getConfig().getInt("profiles.max-profiles", 5);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM profiles WHERE player_uuid = ?")) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) >= maxProfiles) {
                    return null;
                }
            }

            return createProfile(conn, playerUuid, name, false);
        });
    }

    /**
     * Switch a player's active profile.
     */
    public CompletableFuture<Boolean> switchProfile(Player player, int profileId) {
        UUID uuid = player.getUniqueId();
        SkyblockPlayer sbPlayer = getPlayer(uuid);
        if (sbPlayer == null) return CompletableFuture.completedFuture(false);

        // Check cooldown
        long cooldownSeconds = plugin.getConfigManager().getConfig().getLong("profiles.switch-cooldown", 5);
        Long lastSwitch = profileSwitchCooldowns.get(uuid);
        if (lastSwitch != null && System.currentTimeMillis() - lastSwitch < cooldownSeconds * 1000) {
            return CompletableFuture.completedFuture(false);
        }

        // Save current profile first
        savePlayer(uuid);

        return plugin.getDatabaseManager().executeAsync(conn -> {
            // Get the profile
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM profiles WHERE id = ? AND player_uuid = ?")) {
                stmt.setInt(1, profileId);
                stmt.setString(2, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) return false;

                PlayerProfile newProfile = profileFromResultSet(rs);
                loadProfileData(conn, newProfile);

                // Set as active
                setProfileActive(conn, uuid, profileId);

                // Fire event
                PlayerProfile oldProfile = sbPlayer.getActiveProfile();
                ProfileSwitchEvent event = new ProfileSwitchEvent(player, oldProfile, newProfile);
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));

                sbPlayer.setActiveProfile(newProfile);
                profileSwitchCooldowns.put(uuid, System.currentTimeMillis());

                return true;
            }
        });
    }

    /**
     * Delete a profile.
     */
    public CompletableFuture<Boolean> deleteProfile(UUID playerUuid, int profileId) {
        return plugin.getDatabaseManager().executeAsync(conn -> {
            // Check if this is the only profile
            try (PreparedStatement countStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM profiles WHERE player_uuid = ?")) {
                countStmt.setString(1, playerUuid.toString());
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next() && countRs.getInt(1) <= 1) {
                    return false; // Can't delete last profile
                }
            }

            // Delete the profile (cascades to related tables)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM profiles WHERE id = ? AND player_uuid = ?")) {
                stmt.setInt(1, profileId);
                stmt.setString(2, playerUuid.toString());
                int affected = stmt.executeUpdate();
                return affected > 0;
            }
        });
    }

    // Event handlers
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        savePlayer(uuid);
        playerCache.invalidate(uuid);
        profileSwitchCooldowns.remove(uuid);
    }
}
