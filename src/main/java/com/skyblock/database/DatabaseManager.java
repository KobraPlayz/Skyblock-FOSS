package com.skyblock.database;

import com.skyblock.SkyblockPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Manages database connections and operations using HikariCP.
 * Supports both MySQL and SQLite.
 */
public class DatabaseManager {

    private final SkyblockPlugin plugin;
    private HikariDataSource dataSource;
    private ExecutorService executor;
    private boolean isMysql;

    // Schema version for migrations
    private static final int SCHEMA_VERSION = 2;

    public DatabaseManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the database connection pool.
     */
    public boolean initialize() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String type = config.getString("database.type", "SQLITE").toUpperCase();
        isMysql = type.equals("MYSQL");

        // Create executor for async operations
        int maxConcurrent = config.getInt("performance.max-concurrent-operations", 50);
        executor = Executors.newFixedThreadPool(Math.min(maxConcurrent, 10));

        try {
            HikariConfig hikariConfig = new HikariConfig();

            if (isMysql) {
                // MySQL configuration
                ConfigurationSection mysql = config.getConfigurationSection("database.mysql");
                if (mysql == null) {
                    plugin.log(Level.SEVERE, "MySQL configuration section not found!");
                    return false;
                }

                String host = mysql.getString("host", "localhost");
                int port = mysql.getInt("port", 3306);
                String database = mysql.getString("database", "skyblock");
                String username = mysql.getString("username", "root");
                String password = mysql.getString("password", "");

                hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +
                        "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
                hikariConfig.setUsername(username);
                hikariConfig.setPassword(password);
                hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

                // Pool settings from config
                ConfigurationSection pool = mysql.getConfigurationSection("pool");
                if (pool != null) {
                    hikariConfig.setMaximumPoolSize(pool.getInt("maximum-pool-size", 10));
                    hikariConfig.setMinimumIdle(pool.getInt("minimum-idle", 5));
                    hikariConfig.setConnectionTimeout(pool.getLong("connection-timeout", 30000));
                    hikariConfig.setIdleTimeout(pool.getLong("idle-timeout", 600000));
                    hikariConfig.setMaxLifetime(pool.getLong("max-lifetime", 1800000));
                }

                plugin.log(Level.INFO, "Using MySQL database: " + host + ":" + port + "/" + database);

            } else {
                // SQLite configuration
                String fileName = config.getString("database.sqlite.file", "skyblock.db");
                File dbFile = new File(plugin.getDataFolder(), fileName);

                hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
                hikariConfig.setDriverClassName("org.sqlite.JDBC");
                hikariConfig.setMaximumPoolSize(1); // SQLite only supports single connection

                plugin.log(Level.INFO, "Using SQLite database: " + dbFile.getName());
            }

            hikariConfig.setPoolName("SkyblockFOSS-Pool");
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(hikariConfig);

            // Create tables
            createTables();

            // Run migrations
            runMigrations();

            return true;

        } catch (Exception e) {
            plugin.log(Level.SEVERE, "Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create all database tables.
     */
    private void createTables() throws SQLException {
        try (Connection conn = getConnection()) {
            // Schema version table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INT NOT NULL,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Players table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    first_join BIGINT NOT NULL,
                    last_seen BIGINT NOT NULL,
                    settings_json TEXT
                )
            """);

            // Profiles table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS profiles (
                    id INTEGER PRIMARY KEY %s,
                    player_uuid VARCHAR(36) NOT NULL,
                    profile_name VARCHAR(32) NOT NULL,
                    created_at BIGINT NOT NULL,
                    is_active BOOLEAN DEFAULT FALSE,
                    purse DOUBLE DEFAULT 0,
                    bank_balance DOUBLE DEFAULT 0,
                    FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Create index for profiles
            if (isMysql) {
                execute(conn, "CREATE INDEX IF NOT EXISTS idx_profiles_player ON profiles(player_uuid)");
            }

            // Skills table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS skills (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    skill_type VARCHAR(32) NOT NULL,
                    xp DOUBLE DEFAULT 0,
                    level INT DEFAULT 0,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, skill_type)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Collections table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS collections (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    item_type VARCHAR(64) NOT NULL,
                    amount BIGINT DEFAULT 0,
                    highest_tier INT DEFAULT 0,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, item_type)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Transaction log table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    amount DOUBLE NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    description TEXT,
                    timestamp BIGINT NOT NULL,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // =====================================================
            // PHASE 2 TABLES (Create structure but don't use yet)
            // =====================================================

            // Pets table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS pets (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    pet_type VARCHAR(64) NOT NULL,
                    rarity VARCHAR(32) NOT NULL,
                    level INT DEFAULT 1,
                    xp DOUBLE DEFAULT 0,
                    candy_used INT DEFAULT 0,
                    held_item VARCHAR(64),
                    is_active BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Accessories table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS accessories (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    accessory_type VARCHAR(64) NOT NULL,
                    rarity VARCHAR(32) NOT NULL,
                    enrichment VARCHAR(64),
                    data_json TEXT,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Backpacks table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS backpacks (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    slot INT NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    contents_json LONGTEXT,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, slot)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // =====================================================
            // PHASE 3 TABLES (Create structure but don't use yet)
            // =====================================================

            // Minions table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS minions (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    minion_type VARCHAR(64) NOT NULL,
                    tier INT DEFAULT 1,
                    location_json TEXT,
                    fuel VARCHAR(64),
                    fuel_remaining BIGINT DEFAULT 0,
                    upgrade1 VARCHAR(64),
                    upgrade2 VARCHAR(64),
                    storage_json LONGTEXT,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Bazaar orders table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS bazaar_orders (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    item_type VARCHAR(64) NOT NULL,
                    order_type VARCHAR(16) NOT NULL,
                    quantity INT NOT NULL,
                    price_per DOUBLE NOT NULL,
                    filled INT DEFAULT 0,
                    created_at BIGINT NOT NULL,
                    expires_at BIGINT,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Auctions table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS auctions (
                    id INTEGER PRIMARY KEY %s,
                    seller_profile_id INTEGER NOT NULL,
                    item_json LONGTEXT NOT NULL,
                    starting_bid DOUBLE NOT NULL,
                    current_bid DOUBLE DEFAULT 0,
                    highest_bidder_id INTEGER,
                    bin_price DOUBLE,
                    end_time BIGINT NOT NULL,
                    claimed BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (seller_profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Auction bids table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS auction_bids (
                    id INTEGER PRIMARY KEY %s,
                    auction_id INTEGER NOT NULL,
                    bidder_profile_id INTEGER NOT NULL,
                    amount DOUBLE NOT NULL,
                    timestamp BIGINT NOT NULL,
                    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                    FOREIGN KEY (bidder_profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // =====================================================
            // PHASE 4 TABLES (Create structure but don't use yet)
            // =====================================================

            // Dungeon progress table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS dungeon_progress (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    floor VARCHAR(32) NOT NULL,
                    completions INT DEFAULT 0,
                    fastest_time BIGINT,
                    secrets_found INT DEFAULT 0,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, floor)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Dungeon classes table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS dungeon_classes (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    class_type VARCHAR(32) NOT NULL,
                    xp DOUBLE DEFAULT 0,
                    level INT DEFAULT 0,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, class_type)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Slayer progress table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS slayer_progress (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    slayer_type VARCHAR(32) NOT NULL,
                    xp DOUBLE DEFAULT 0,
                    level INT DEFAULT 0,
                    boss_kills_json TEXT,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, slayer_type)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Quests table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS quests (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    quest_id VARCHAR(64) NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    progress_json TEXT,
                    started_at BIGINT,
                    completed_at BIGINT,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, quest_id)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Fairy souls table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS fairy_souls (
                    id INTEGER PRIMARY KEY %s,
                    profile_id INTEGER NOT NULL,
                    location_id VARCHAR(64) NOT NULL,
                    collected_at BIGINT NOT NULL,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(profile_id, location_id)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Events table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS events (
                    id INTEGER PRIMARY KEY %s,
                    event_type VARCHAR(64) NOT NULL,
                    start_time BIGINT NOT NULL,
                    end_time BIGINT NOT NULL,
                    data_json TEXT
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Event participation table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS event_participation (
                    id INTEGER PRIMARY KEY %s,
                    event_id INTEGER NOT NULL,
                    profile_id INTEGER NOT NULL,
                    score DOUBLE DEFAULT 0,
                    rewards_claimed BOOLEAN DEFAULT FALSE,
                    data_json TEXT,
                    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
                    UNIQUE(event_id, profile_id)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // =====================================================
            // PHASE 1.5 TABLES (Island, Garden, Co-op, Furniture)
            // =====================================================

            // Islands table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS islands (
                    id VARCHAR(36) PRIMARY KEY,
                    profile_id INTEGER NOT NULL UNIQUE,
                    world_name VARCHAR(64) NOT NULL,
                    spawn_x DOUBLE DEFAULT 0,
                    spawn_y DOUBLE DEFAULT 100,
                    spawn_z DOUBLE DEFAULT 0,
                    spawn_yaw FLOAT DEFAULT 0,
                    spawn_pitch FLOAT DEFAULT 0,
                    size INT DEFAULT 160,
                    created_at BIGINT NOT NULL,
                    last_accessed BIGINT NOT NULL,
                    is_public BOOLEAN DEFAULT FALSE,
                    pvp_enabled BOOLEAN DEFAULT FALSE,
                    guest_limit INT DEFAULT 5,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """);

            // Island members table (for co-op)
            execute(conn, """
                CREATE TABLE IF NOT EXISTS island_members (
                    id INTEGER PRIMARY KEY %s,
                    island_id VARCHAR(36) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
                    joined_at BIGINT NOT NULL,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE,
                    UNIQUE(island_id, player_uuid)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Island settings table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS island_settings (
                    id INTEGER PRIMARY KEY %s,
                    island_id VARCHAR(36) NOT NULL,
                    setting_key VARCHAR(64) NOT NULL,
                    setting_value TEXT,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE,
                    UNIQUE(island_id, setting_key)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Island visitors log
            execute(conn, """
                CREATE TABLE IF NOT EXISTS island_visitors (
                    id INTEGER PRIMARY KEY %s,
                    island_id VARCHAR(36) NOT NULL,
                    visitor_uuid VARCHAR(36) NOT NULL,
                    visit_count INT DEFAULT 1,
                    total_time_seconds BIGINT DEFAULT 0,
                    last_visit BIGINT NOT NULL,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE,
                    UNIQUE(island_id, visitor_uuid)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Island banned players
            execute(conn, """
                CREATE TABLE IF NOT EXISTS island_bans (
                    id INTEGER PRIMARY KEY %s,
                    island_id VARCHAR(36) NOT NULL,
                    banned_uuid VARCHAR(36) NOT NULL,
                    banned_by VARCHAR(36) NOT NULL,
                    banned_at BIGINT NOT NULL,
                    reason TEXT,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE,
                    UNIQUE(island_id, banned_uuid)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Co-op invites table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS coop_invites (
                    id INTEGER PRIMARY KEY %s,
                    island_id VARCHAR(36) NOT NULL,
                    inviter_uuid VARCHAR(36) NOT NULL,
                    invitee_uuid VARCHAR(36) NOT NULL,
                    invited_at BIGINT NOT NULL,
                    expires_at BIGINT NOT NULL,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Co-op kick votes
            execute(conn, """
                CREATE TABLE IF NOT EXISTS coop_kick_votes (
                    id INTEGER PRIMARY KEY %s,
                    island_id VARCHAR(36) NOT NULL,
                    target_uuid VARCHAR(36) NOT NULL,
                    voter_uuid VARCHAR(36) NOT NULL,
                    voted_at BIGINT NOT NULL,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE,
                    UNIQUE(island_id, target_uuid, voter_uuid)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Gardens table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS gardens (
                    id VARCHAR(36) PRIMARY KEY,
                    profile_id INTEGER NOT NULL UNIQUE,
                    world_name VARCHAR(64),
                    garden_level INT DEFAULT 1,
                    garden_xp DOUBLE DEFAULT 0,
                    copper_balance BIGINT DEFAULT 0,
                    compost_balance BIGINT DEFAULT 0,
                    unlocked_at BIGINT NOT NULL,
                    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
            """);

            // Garden plots table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS garden_plots (
                    id INTEGER PRIMARY KEY %s,
                    garden_id VARCHAR(36) NOT NULL,
                    plot_number INT NOT NULL,
                    unlocked BOOLEAN DEFAULT FALSE,
                    cleaned BOOLEAN DEFAULT FALSE,
                    preset_type VARCHAR(32),
                    crop_type VARCHAR(32),
                    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
                    UNIQUE(garden_id, plot_number)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Garden crop upgrades
            execute(conn, """
                CREATE TABLE IF NOT EXISTS garden_crop_upgrades (
                    id INTEGER PRIMARY KEY %s,
                    garden_id VARCHAR(36) NOT NULL,
                    crop_type VARCHAR(32) NOT NULL,
                    upgrade_level INT DEFAULT 0,
                    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
                    UNIQUE(garden_id, crop_type)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Garden milestones
            execute(conn, """
                CREATE TABLE IF NOT EXISTS garden_milestones (
                    id INTEGER PRIMARY KEY %s,
                    garden_id VARCHAR(36) NOT NULL,
                    crop_type VARCHAR(32) NOT NULL,
                    amount_farmed BIGINT DEFAULT 0,
                    milestone_tier INT DEFAULT 0,
                    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
                    UNIQUE(garden_id, crop_type)
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Garden visitors (NPCs)
            execute(conn, """
                CREATE TABLE IF NOT EXISTS garden_visitors (
                    id INTEGER PRIMARY KEY %s,
                    garden_id VARCHAR(36) NOT NULL,
                    visitor_type VARCHAR(64) NOT NULL,
                    request_item VARCHAR(64) NOT NULL,
                    request_amount INT NOT NULL,
                    reward_copper BIGINT DEFAULT 0,
                    reward_items_json TEXT,
                    spawned_at BIGINT NOT NULL,
                    expires_at BIGINT NOT NULL,
                    completed BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE
                )
            """.formatted(isMysql ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Furniture placements table
            execute(conn, """
                CREATE TABLE IF NOT EXISTS furniture (
                    id VARCHAR(36) PRIMARY KEY,
                    island_id VARCHAR(36) NOT NULL,
                    furniture_type VARCHAR(64) NOT NULL,
                    x DOUBLE NOT NULL,
                    y DOUBLE NOT NULL,
                    z DOUBLE NOT NULL,
                    yaw FLOAT DEFAULT 0,
                    data_json TEXT,
                    placed_at BIGINT NOT NULL,
                    placed_by VARCHAR(36) NOT NULL,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
                )
            """);

            // Create indexes for Phase 1.5 tables
            if (isMysql) {
                execute(conn, "CREATE INDEX IF NOT EXISTS idx_islands_profile ON islands(profile_id)");
                execute(conn, "CREATE INDEX IF NOT EXISTS idx_island_members_island ON island_members(island_id)");
                execute(conn, "CREATE INDEX IF NOT EXISTS idx_island_visitors_island ON island_visitors(island_id)");
                execute(conn, "CREATE INDEX IF NOT EXISTS idx_gardens_profile ON gardens(profile_id)");
                execute(conn, "CREATE INDEX IF NOT EXISTS idx_garden_plots_garden ON garden_plots(garden_id)");
                execute(conn, "CREATE INDEX IF NOT EXISTS idx_furniture_island ON furniture(island_id)");
            }

            plugin.log(Level.INFO, "Database tables created successfully.");
        }
    }

    /**
     * Run database migrations.
     */
    private void runMigrations() throws SQLException {
        int currentVersion = getCurrentSchemaVersion();

        if (currentVersion < SCHEMA_VERSION) {
            plugin.log(Level.INFO, "Running database migrations from v" + currentVersion + " to v" + SCHEMA_VERSION);

            // Add migration logic here as needed
            // Example:
            // if (currentVersion < 2) { migrateToV2(); }

            setSchemaVersion(SCHEMA_VERSION);
        }
    }

    private int getCurrentSchemaVersion() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT version FROM schema_version ORDER BY version DESC LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("version");
            }
        } catch (SQLException e) {
            // Table might not exist yet
        }
        return 0;
    }

    private void setSchemaVersion(int version) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO schema_version (version) VALUES (?)")) {
            stmt.setInt(1, version);
            stmt.executeUpdate();
        }
    }

    /**
     * Execute a SQL statement.
     */
    private void execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Get a database connection.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Execute an async database operation.
     */
    public <T> CompletableFuture<T> executeAsync(DatabaseOperation<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                return operation.execute(conn);
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Database operation failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Execute an async update operation.
     */
    public CompletableFuture<Void> executeUpdateAsync(DatabaseUpdateOperation operation) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                operation.execute(conn);
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Database update failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Shutdown the database connection pool.
     */
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        plugin.log(Level.INFO, "Database connections closed.");
    }

    /**
     * Check if using MySQL.
     */
    public boolean isMysql() {
        return isMysql;
    }

    /**
     * Functional interface for database operations that return a value.
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute(Connection conn) throws SQLException;
    }

    /**
     * Functional interface for database operations that don't return a value.
     */
    @FunctionalInterface
    public interface DatabaseUpdateOperation {
        void execute(Connection conn) throws SQLException;
    }
}
