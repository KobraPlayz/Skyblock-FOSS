# SkyblockFOSS Coding Standards

This document defines all coding standards, patterns, and conventions established during Phase 1 and Phase 1.5 development. **All future development MUST follow these standards** to maintain consistency and code quality.

---

## Table of Contents

1. [Package Structure](#1-package-structure)
2. [Class Organization](#2-class-organization)
3. [Manager Pattern](#3-manager-pattern)
4. [Database Standards](#4-database-standards)
5. [GUI Framework](#5-gui-framework)
6. [Configuration System](#6-configuration-system)
7. [Module System](#7-module-system)
8. [Event System](#8-event-system)
9. [Command Structure](#9-command-structure)
10. [Caching Strategy](#10-caching-strategy)
11. [Error Handling](#11-error-handling)
12. [Documentation Standards](#12-documentation-standards)
13. [What NOT To Do](#13-what-not-to-do)
14. [Integration Patterns](#14-integration-patterns)
15. [Testing Protocol](#15-testing-protocol)

---

## 1. Package Structure

### Root Package
```
com.skyblock/
├── SkyblockPlugin.java          # Main plugin class - SINGLETON
├── api/                         # Public API for other plugins
│   ├── SkyblockAPI.java
│   └── events/                  # Custom Bukkit events
├── commands/                    # All command executors
├── config/                      # Configuration management
├── database/                    # Database access layer
├── gui/                         # GUI framework
│   ├── menus/                   # Individual menu implementations
│   └── utils/                   # GUI utilities (ItemBuilder, etc.)
├── modules/                     # Module system
├── player/                      # Player data management
├── utils/                       # General utilities
└── [feature]/                   # Feature-specific packages
```

### Feature Package Structure
Each major feature gets its own package:
```
com.skyblock.island/
├── Island.java                  # Data class
├── IslandRole.java              # Enum (if needed)
├── IslandManager.java           # Manager class
└── IslandProtectionListener.java # Listeners specific to this feature
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Packages | lowercase, singular | `com.skyblock.island` |
| Classes | PascalCase | `IslandManager` |
| Interfaces | PascalCase, descriptive | `Configurable` |
| Enums | PascalCase | `IslandRole` |
| Constants | UPPER_SNAKE_CASE | `MAX_ISLAND_SIZE` |
| Methods | camelCase, verb-first | `getIsland()`, `createIsland()` |
| Variables | camelCase | `islandCache` |
| Config keys | kebab-case | `max-island-size` |

---

## 2. Class Organization

### Standard Class Order
```java
public class ExampleManager {

    // 1. Static fields (constants first)
    private static final int MAX_CACHE_SIZE = 1000;
    private static ExampleManager instance;

    // 2. Instance fields
    private final SkyblockPlugin plugin;
    private final Cache<UUID, Example> cache;

    // 3. Constructor
    public ExampleManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.cache = buildCache();
    }

    // 4. Public methods (API)
    public Example getExample(UUID uuid) { }
    public void saveExample(Example example) { }

    // 5. Package-private/Protected methods
    void internalMethod() { }

    // 6. Private methods
    private Cache<UUID, Example> buildCache() { }

    // 7. Inner classes/enums (if small, otherwise separate file)
    public enum Status { ACTIVE, INACTIVE }
}
```

### Data Class Pattern
```java
public class Island {

    // Fields - prefer final where possible
    private final UUID id;
    private final UUID ownerUuid;
    private String worldName;
    private double spawnX, spawnY, spawnZ;
    private int sizeX, sizeZ;

    // Settings stored as Map for flexibility
    private final Map<String, Object> settings;

    // Collections for related data
    private final Set<UUID> members;
    private final Set<UUID> bannedPlayers;

    // Constructor - initialize all collections
    public Island(UUID id, UUID ownerUuid) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.settings = new HashMap<>();
        this.members = new HashSet<>();
        this.bannedPlayers = new HashSet<>();
    }

    // Type-safe setting accessors
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, T defaultValue) {
        Object value = settings.get(key);
        if (value == null) return defaultValue;
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }

    // Standard getters/setters
    // ...
}
```

---

## 3. Manager Pattern

### Manager Class Template
Every manager MUST follow this pattern:

```java
public class FeatureManager implements Listener {

    private final SkyblockPlugin plugin;
    private final Cache<UUID, Feature> cache;

    public FeatureManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.cache = Caffeine.newBuilder()
                .maximumSize(getConfiguredCacheSize())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
    }

    // ==================== PUBLIC API ====================

    /**
     * Get feature data, loading from cache or database.
     *
     * @param uuid The player UUID
     * @return The feature data, or null if not found
     */
    public Feature getFeature(UUID uuid) {
        // Try cache first
        Feature cached = cache.getIfPresent(uuid);
        if (cached != null) {
            return cached;
        }

        // Load from database (blocking for simplicity in sync context)
        Feature loaded = loadFromDatabase(uuid);
        if (loaded != null) {
            cache.put(uuid, loaded);
        }
        return loaded;
    }

    /**
     * Get feature data asynchronously.
     * PREFERRED METHOD for non-critical paths.
     */
    public CompletableFuture<Feature> getFeatureAsync(UUID uuid) {
        Feature cached = cache.getIfPresent(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            Feature loaded = loadFromDatabase(uuid);
            if (loaded != null) {
                cache.put(uuid, loaded);
            }
            return loaded;
        });
    }

    /**
     * Save feature data asynchronously.
     * ALWAYS use async for saves.
     */
    public CompletableFuture<Void> saveFeature(Feature feature) {
        cache.put(feature.getOwnerUuid(), feature);
        return CompletableFuture.runAsync(() -> saveToDatabase(feature));
    }

    // ==================== LIFECYCLE ====================

    /**
     * Called on plugin shutdown.
     * Save all cached data and cleanup.
     */
    public void shutdown() {
        plugin.log(Level.INFO, "Shutting down FeatureManager...");

        // Save all cached entries
        cache.asMap().values().forEach(this::saveToDatabase);
        cache.invalidateAll();
    }

    // ==================== PRIVATE METHODS ====================

    private Feature loadFromDatabase(UUID uuid) {
        // Database query implementation
    }

    private void saveToDatabase(Feature feature) {
        // Database save implementation
    }

    private int getConfiguredCacheSize() {
        return plugin.getConfigManager()
                .getConfig()
                .getInt("feature.cache-size", 500);
    }
}
```

### Manager Registration in Main Plugin
```java
// In SkyblockPlugin.java onEnable()

// Initialize managers in dependency order
log(Level.INFO, "Initializing managers...");
playerManager = new PlayerManager(this);
itemManager = new ItemManager(this);
skillManager = new SkillManager(this);
// ... Phase 1 managers

// Phase 1.5 managers
log(Level.INFO, "Initializing Phase 1.5 managers...");
worldManager = new WorldManager(this);
islandManager = new IslandManager(this);
coopManager = new CoopManager(this);
gardenManager = new GardenManager(this);
furnitureManager = new FurnitureManager(this);

// Phase 2 managers would go here
// minionManager = new MinionManager(this);
// petManager = new PetManager(this);
```

---

## 4. Database Standards

### Schema Design Rules

1. **Every table MUST have a primary key**
2. **Use UUID for player references** (VARCHAR(36) or BINARY(16))
3. **Include timestamps** for data lifecycle management
4. **Create indexes** for frequently queried columns
5. **Use foreign keys** where appropriate (with CASCADE)

### Table Creation Template
```java
private void createFeatureTable() {
    String sql = """
        CREATE TABLE IF NOT EXISTS features (
            id VARCHAR(36) PRIMARY KEY,
            owner_uuid VARCHAR(36) NOT NULL,
            data_field VARCHAR(255),
            numeric_field BIGINT DEFAULT 0,
            boolean_field BOOLEAN DEFAULT FALSE,
            json_data TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

            INDEX idx_owner (owner_uuid),
            FOREIGN KEY (owner_uuid) REFERENCES players(uuid) ON DELETE CASCADE
        )
        """;

    try (Connection conn = plugin.getDatabaseManager().getConnection();
         Statement stmt = conn.createStatement()) {
        stmt.execute(sql);
    } catch (SQLException e) {
        plugin.log(Level.SEVERE, "Failed to create features table: " + e.getMessage());
    }
}
```

### Async Query Pattern
**ALL database operations MUST be async except during startup/shutdown.**

```java
// CORRECT - Async query
public CompletableFuture<Feature> loadFeatureAsync(UUID uuid) {
    return CompletableFuture.supplyAsync(() -> {
        String sql = "SELECT * FROM features WHERE owner_uuid = ?";

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return parseFeature(rs);
            }
            return null;

        } catch (SQLException e) {
            plugin.log(Level.WARNING, "Failed to load feature: " + e.getMessage());
            return null;
        }
    });
}

// CORRECT - Async save
public CompletableFuture<Void> saveFeatureAsync(Feature feature) {
    return CompletableFuture.runAsync(() -> {
        String sql = """
            INSERT INTO features (id, owner_uuid, data_field, numeric_field)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                data_field = VALUES(data_field),
                numeric_field = VALUES(numeric_field)
            """;

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, feature.getId().toString());
            stmt.setString(2, feature.getOwnerUuid().toString());
            stmt.setString(3, feature.getDataField());
            stmt.setLong(4, feature.getNumericField());
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to save feature: " + e.getMessage());
        }
    });
}

// WRONG - Sync query on main thread
public Feature loadFeatureSync(UUID uuid) {
    // DON'T DO THIS - blocks main thread
}
```

### Batch Operations
For bulk operations, use batch updates:
```java
public CompletableFuture<Void> saveAllFeatures(Collection<Feature> features) {
    return CompletableFuture.runAsync(() -> {
        String sql = "UPDATE features SET data_field = ? WHERE id = ?";

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Feature feature : features) {
                stmt.setString(1, feature.getDataField());
                stmt.setString(2, feature.getId().toString());
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Batch save failed: " + e.getMessage());
        }
    });
}
```

### Schema Versioning
```java
// In DatabaseManager.java
private static final int SCHEMA_VERSION = 3; // Increment for Phase 2

private void checkSchemaVersion() {
    int currentVersion = getCurrentSchemaVersion();

    if (currentVersion < SCHEMA_VERSION) {
        migrateSchema(currentVersion, SCHEMA_VERSION);
    }
}

private void migrateSchema(int from, int to) {
    for (int version = from + 1; version <= to; version++) {
        switch (version) {
            case 2 -> migrateToV2(); // Phase 1.5 tables
            case 3 -> migrateToV3(); // Phase 2 tables
        }
    }
    updateSchemaVersion(to);
}
```

---

## 5. GUI Framework

### AbstractGUI Usage
ALL menus MUST extend `AbstractGUI`:

```java
public class FeatureMenu extends AbstractGUI {

    private final UUID targetUuid; // Store any context needed

    public FeatureMenu(SkyblockPlugin plugin, UUID targetUuid) {
        super(plugin, "&8Feature Menu", 6); // Title, rows
        this.targetUuid = targetUuid;
    }

    @Override
    protected void build(Player player) {
        // Always fill border first for consistency
        fillBorder(createFiller());

        // Header item at slot 4 (top center)
        setItem(4, new ItemBuilder(Material.DIAMOND)
                .name("&b&lFeature Title")
                .lore(
                        "&7Description line 1",
                        "&7Description line 2",
                        "",
                        "&7Some stat: &e" + getSomeStat()
                )
                .build());

        // Content items
        setItem(20, createContentItem(), event -> {
            handleContentClick(player);
        });

        // Back button at slot 45 (bottom left)
        setItem(45, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new ParentMenu(plugin));
        });

        // Close button at slot 53 (bottom right)
        setItem(53, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private ItemStack createContentItem() {
        return new ItemBuilder(Material.PAPER)
                .name("&eContent Item")
                .lore("&7Click to do something")
                .build();
    }

    private void handleContentClick(Player player) {
        // Handle the click action
        playClickSound(player);
        // ... do something
    }
}
```

### GUI Slot Layout Reference
```
Standard 6-row (54 slots) layout:

Row 1 (Border):  [0 ][1 ][2 ][3 ][4*][5 ][6 ][7 ][8 ]   * = Header slot
Row 2 (Border):  [9 ][10][11][12][13][14][15][16][17]
Row 3 (Content): [18][19][20][21][22][23][24][25][26]
Row 4 (Content): [27][28][29][30][31][32][33][34][35]
Row 5 (Content): [36][37][38][39][40][41][42][43][44]
Row 6 (Border):  [45*][46][47][48][49][50][51][52][53*] * = Back/Close

Common slot assignments:
- 4: Title/header item
- 20, 22, 24: Main action buttons (3-column layout)
- 28, 30, 32, 34: Secondary actions (4-column layout)
- 45: Back button
- 49: Admin panel (if applicable)
- 53: Close button
```

### ItemBuilder Usage
```java
// Simple item
new ItemBuilder(Material.DIAMOND)
        .name("&b&lItem Name")
        .build();

// With lore
new ItemBuilder(Material.SWORD)
        .name("&c&lWeapon")
        .lore(
                "&7Line 1",
                "&7Line 2",
                "",
                "&eClick to use!"
        )
        .hideFlags()
        .build();

// With amount
new ItemBuilder(Material.GOLD_INGOT)
        .name("&6Gold")
        .amount(64)
        .build();

// Skull with custom texture
new ItemBuilder(Material.PLAYER_HEAD)
        .name("&ePlayer Head")
        .skullOwner(playerName)
        .build();
```

---

## 6. Configuration System

### Config File Structure
Every config file MUST have:
1. Header comment explaining the file
2. Logical grouping of settings
3. Default values that are sensible
4. Comments for non-obvious settings

```yaml
# ===========================================
# Feature Configuration
# ===========================================
# This file controls the feature system.
# Reload with /sbadmin reload

feature:
  # Whether this feature is enabled
  enabled: true

  # Maximum items per player
  max-items: 100

  # Cache settings
  cache:
    size: 500
    expire-minutes: 30

  # Specific feature settings
  settings:
    option-a: true
    option-b: 50
    option-c: "default"

# Messages for this feature
messages:
  feature-created: "&aFeature created successfully!"
  feature-deleted: "&cFeature deleted."
  error-max-reached: "&cYou have reached the maximum!"
```

### ConfigManager Integration
```java
// In ConfigManager.java - Add constant
public static final String FEATURE = "feature";

// In loadAllConfigs() - Add loading
saveDefaultConfig(FEATURE);
loadConfig(FEATURE);

// Add getter method
public FileConfiguration getFeatureConfig() {
    return configs.get(FEATURE);
}
```

### Reading Config Values
```java
// CORRECT - With defaults
int maxItems = plugin.getConfigManager()
        .getFeatureConfig()
        .getInt("feature.max-items", 100);

boolean enabled = plugin.getConfigManager()
        .getFeatureConfig()
        .getBoolean("feature.enabled", true);

String message = plugin.getConfigManager()
        .getFeatureConfig()
        .getString("messages.feature-created", "&aCreated!");

// CORRECT - Check if enabled before processing
if (!plugin.getModuleManager().isModuleEnabled("feature")) {
    return;
}
```

---

## 7. Module System

### Adding a New Module
1. Add to `modules.yml`:
```yaml
modules:
  # ... existing modules

  # Phase 2 modules
  minions:
    enabled: true
    description: "Automated resource gathering"

  pets:
    enabled: true
    description: "Collectible companions with bonuses"
```

2. Check module status before executing:
```java
// In command or listener
if (!plugin.getModuleManager().isModuleEnabled("minions")) {
    player.sendMessage(ColorUtils.colorize("&cMinions are currently disabled!"));
    return;
}
```

3. Register listeners conditionally:
```java
// In SkyblockPlugin.registerListeners()
if (moduleManager.isModuleEnabled("minions")) {
    getServer().getPluginManager().registerEvents(minionManager, this);
}
```

### Coming Soon Features
For features planned but not implemented:
```java
// In GUI
if (plugin.getModuleManager().isComingSoon("dungeons")) {
    setItem(slot, new ItemBuilder(Material.MOSSY_COBBLESTONE)
            .name("&8Dungeons")
            .lore("&8Coming in Phase 4!")
            .build());
}
```

---

## 8. Event System

### Creating Custom Events
```java
// In com.skyblock.api.events/
public class FeatureCreateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Feature feature;
    private boolean cancelled = false;

    public FeatureCreateEvent(Player player, Feature feature) {
        this.player = player;
        this.feature = feature;
    }

    public Player getPlayer() {
        return player;
    }

    public Feature getFeature() {
        return feature;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
```

### Firing Events
```java
// Before creating feature
FeatureCreateEvent event = new FeatureCreateEvent(player, feature);
Bukkit.getPluginManager().callEvent(event);

if (event.isCancelled()) {
    player.sendMessage("&cFeature creation was cancelled!");
    return;
}

// Proceed with creation
```

### Listening to Events
```java
public class FeatureListener implements Listener {

    private final SkyblockPlugin plugin;

    public FeatureListener(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFeatureCreate(FeatureCreateEvent event) {
        // Handle event
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // Only fires if not cancelled by lower priority handlers
    }
}
```

---

## 9. Command Structure

### Command Class Template
```java
public class FeatureCommand implements CommandExecutor, TabCompleter {

    private final SkyblockPlugin plugin;

    public FeatureCommand(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                            String label, String[] args) {

        // Check if player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        // Check permission
        if (!player.hasPermission("skyblock.feature")) {
            player.sendMessage(ColorUtils.colorize("&cNo permission!"));
            return true;
        }

        // Check module enabled
        if (!plugin.getModuleManager().isModuleEnabled("feature")) {
            player.sendMessage(ColorUtils.colorize("&cThis feature is disabled!"));
            return true;
        }

        // No args - default action
        if (args.length == 0) {
            handleDefault(player);
            return true;
        }

        // Subcommands
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "list" -> handleList(player);
            case "help" -> sendHelp(player);
            default -> {
                player.sendMessage(ColorUtils.colorize(
                        "&cUnknown subcommand. Use /feature help"));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                       String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "list", "help")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            // Return list of feature names
            return getFeatureNames().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void handleDefault(Player player) {
        // Open GUI or show info
        plugin.getGuiManager().openGUI(player, new FeatureMenu(plugin, player.getUniqueId()));
    }

    private void handleCreate(Player player, String[] args) {
        // Implementation
    }

    private void handleDelete(Player player, String[] args) {
        // Implementation
    }

    private void handleList(Player player) {
        // Implementation
    }

    private void sendHelp(Player player) {
        player.sendMessage(ColorUtils.colorize("&6&lFeature Commands:"));
        player.sendMessage(ColorUtils.colorize("&e/feature &7- Open feature menu"));
        player.sendMessage(ColorUtils.colorize("&e/feature create &7- Create a feature"));
        player.sendMessage(ColorUtils.colorize("&e/feature delete <name> &7- Delete a feature"));
        player.sendMessage(ColorUtils.colorize("&e/feature list &7- List all features"));
    }
}
```

### Registering Commands
```java
// In plugin.yml
commands:
  feature:
    description: Manage features
    aliases: [feat, f]
    permission: skyblock.feature

// In SkyblockPlugin.registerCommands()
getCommand("feature").setExecutor(new FeatureCommand(this));
```

---

## 10. Caching Strategy

### When to Cache
- **DO cache**: Player data, island data, frequently accessed game data
- **DON'T cache**: One-time lookups, rarely accessed data, large datasets

### Cache Configuration
```java
// Standard cache for player-owned data
Cache<UUID, Feature> cache = Caffeine.newBuilder()
        .maximumSize(1000)                    // Max entries
        .expireAfterAccess(30, TimeUnit.MINUTES)  // Expire if not accessed
        .build();

// Cache for frequently read, rarely written data
Cache<String, ConfigData> configCache = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(5, TimeUnit.MINUTES)  // Refresh periodically
        .build();

// Cache with removal listener (for saving)
Cache<UUID, Feature> savingCache = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .removalListener((key, value, cause) -> {
            if (value != null && cause.wasEvicted()) {
                saveToDatabase((Feature) value);
            }
        })
        .build();
```

### Cache Invalidation
```java
// Single entry
cache.invalidate(uuid);

// All entries
cache.invalidateAll();

// Conditional (during save)
public void saveFeature(Feature feature) {
    saveToDatabase(feature);
    cache.put(feature.getOwnerUuid(), feature); // Update cache
}
```

---

## 11. Error Handling

### Logging Standards
```java
// INFO - Normal operations
plugin.log(Level.INFO, "Loaded " + count + " features");

// WARNING - Recoverable issues
plugin.log(Level.WARNING, "Feature not found for " + uuid + ", creating new");

// SEVERE - Critical errors
plugin.log(Level.SEVERE, "Database connection failed: " + e.getMessage());
e.printStackTrace(); // Include stack trace for severe errors
```

### Exception Handling Pattern
```java
public Feature loadFeature(UUID uuid) {
    try {
        return loadFromDatabase(uuid);
    } catch (SQLException e) {
        plugin.log(Level.WARNING, "Failed to load feature for " + uuid + ": " + e.getMessage());
        return createDefaultFeature(uuid); // Graceful fallback
    } catch (Exception e) {
        plugin.log(Level.SEVERE, "Unexpected error loading feature: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
```

### Never Throw to Player
```java
// WRONG
public void doSomething(Player player) throws SQLException {
    // Player sees ugly error
}

// CORRECT
public void doSomething(Player player) {
    try {
        // Do the thing
    } catch (Exception e) {
        player.sendMessage(ColorUtils.colorize("&cAn error occurred. Please try again."));
        plugin.log(Level.WARNING, "Error in doSomething: " + e.getMessage());
    }
}
```

---

## 12. Documentation Standards

### JavaDoc Requirements
**Required for:**
- All public methods
- All public classes
- Complex private methods

```java
/**
 * Manages all feature-related operations including creation,
 * deletion, and persistence.
 *
 * <p>Features are cached in memory and persisted to the database
 * asynchronously. The cache has a maximum size of 1000 entries
 * and entries expire after 30 minutes of inactivity.</p>
 *
 * @since Phase 2.0
 * @see Feature
 * @see FeatureCommand
 */
public class FeatureManager {

    /**
     * Gets a feature by its owner's UUID.
     *
     * <p>This method first checks the cache, then falls back to
     * database lookup if not found. The result is cached for
     * future access.</p>
     *
     * @param uuid the UUID of the feature owner
     * @return the Feature, or null if not found
     * @throws IllegalArgumentException if uuid is null
     */
    public Feature getFeature(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        // Implementation
    }
}
```

### Inline Comments
```java
// GOOD - Explains WHY
// Use insertion order map to maintain display order in GUI
Map<String, Feature> features = new LinkedHashMap<>();

// GOOD - Explains complex logic
// Calculate farming fortune: base + (upgrade_level * 5) + (milestone_bonus)
int fortune = BASE_FORTUNE + (upgradeLevel * FORTUNE_PER_LEVEL) + milestoneBonus;

// BAD - States the obvious
// Get the player
Player player = event.getPlayer();

// BAD - Outdated comment
// This adds 10 to the value (actually adds 20 now)
value += 20;
```

### Config File Comments
```yaml
# ===========================================
# Section Header
# ===========================================

setting:
  # What this setting does
  # Default: value
  # Valid range: 1-100
  option: 50
```

---

## 13. What NOT To Do

### Performance Pitfalls

```java
// DON'T: Sync database on main thread
public void onBlockBreak(BlockBreakEvent event) {
    saveToDatabase(data); // BLOCKS MAIN THREAD
}

// DO: Async database operations
public void onBlockBreak(BlockBreakEvent event) {
    CompletableFuture.runAsync(() -> saveToDatabase(data));
}
```

```java
// DON'T: Create new objects in hot paths
@EventHandler
public void onMove(PlayerMoveEvent event) {
    Location loc = new Location(world, x, y, z); // Creates garbage every tick
}

// DO: Reuse objects or use primitives
@EventHandler
public void onMove(PlayerMoveEvent event) {
    Location from = event.getFrom();
    if (from.getBlockX() != to.getBlockX()) { // Compare primitives
        // Handle movement
    }
}
```

```java
// DON'T: Iterate all players frequently
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    for (Player p : Bukkit.getOnlinePlayers()) {
        updatePlayerData(p); // Every tick for every player
    }
}, 0L, 1L);

// DO: Process in batches or on-demand
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    // Process a few players per tick
    // Or only process when data changes
}, 0L, 20L);
```

### Code Smells to Avoid

```java
// DON'T: God classes
public class EverythingManager {
    // 5000 lines handling everything
}

// DO: Single responsibility
public class IslandManager { /* Island stuff only */ }
public class GardenManager { /* Garden stuff only */ }
```

```java
// DON'T: Magic numbers
if (level >= 5) { // What is 5?
    unlockGarden();
}

// DO: Named constants
private static final int GARDEN_UNLOCK_LEVEL = 5;

if (level >= GARDEN_UNLOCK_LEVEL) {
    unlockGarden();
}
```

```java
// DON'T: Catch and ignore
try {
    doSomething();
} catch (Exception e) {
    // Silently ignored - bugs will be invisible
}

// DO: Log or handle
try {
    doSomething();
} catch (Exception e) {
    plugin.log(Level.WARNING, "Failed to do something: " + e.getMessage());
}
```

### Anti-Patterns

```java
// DON'T: Static abuse
public class FeatureManager {
    public static Feature getFeature(UUID uuid) { // No instance needed??
        return SkyblockPlugin.getInstance().getFeatureManager().get(uuid);
    }
}

// DO: Instance methods through plugin
Feature feature = plugin.getFeatureManager().getFeature(uuid);
```

```java
// DON'T: Hardcoded messages
player.sendMessage("§cYou don't have permission!");

// DO: Configurable messages
player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
```

---

## 14. Integration Patterns

### Extending Existing Managers

When adding to existing systems, **EXTEND, don't replace**:

```java
// In existing PlayerManager - ADD method, don't modify existing
public class PlayerManager {

    // Existing methods stay unchanged...

    // NEW: Phase 2 addition
    public List<Minion> getPlayerMinions(UUID uuid) {
        return plugin.getMinionManager().getMinions(uuid);
    }
}
```

### Cross-Manager Communication

```java
// FeatureManager needs skill data
public class FeatureManager {

    public void processFeature(Player player) {
        // Get data from other manager through plugin
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        int farmingLevel = sbPlayer.getSkillLevel(SkillType.FARMING);

        // Use the data
        int bonus = calculateBonus(farmingLevel);
    }
}
```

### Adding to Existing GUIs

```java
// In SkyblockMenu.java - ADD slot, keep existing layout

// Existing slots: 20, 22, 24 (Skills, Collections, Profile)
// Existing slots: 28, 30, 34 (Island, Purse, Garden) - Phase 1.5

// Phase 2 additions - use empty slots
setItem(32, new ItemBuilder(Material.ARMOR_STAND)
        .name("&d&lMinions")
        .lore("&7Manage your minions!")
        .build(), event -> {
    plugin.getGuiManager().openGUI(player, new MinionsMenu(plugin, player.getUniqueId()));
});

setItem(38, new ItemBuilder(Material.BONE)
        .name("&a&lPets")
        .lore("&7View your pets!")
        .build(), event -> {
    plugin.getGuiManager().openGUI(player, new PetsMenu(plugin, player.getUniqueId()));
});
```

### Database Migration

When adding Phase 2 tables:

```java
// In DatabaseManager.java

private static final int SCHEMA_VERSION = 3; // Was 2 for Phase 1.5

private void createPhase2Tables() {
    // Minions table
    executeUpdate("""
        CREATE TABLE IF NOT EXISTS minions (
            id VARCHAR(36) PRIMARY KEY,
            island_id VARCHAR(36) NOT NULL,
            type VARCHAR(50) NOT NULL,
            level INT DEFAULT 1,
            location_world VARCHAR(100),
            location_x DOUBLE,
            location_y DOUBLE,
            location_z DOUBLE,
            storage TEXT,
            last_collection TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

            INDEX idx_island (island_id),
            FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
        )
        """);

    // Pets table
    executeUpdate("""
        CREATE TABLE IF NOT EXISTS pets (
            id VARCHAR(36) PRIMARY KEY,
            owner_uuid VARCHAR(36) NOT NULL,
            type VARCHAR(50) NOT NULL,
            rarity VARCHAR(20) NOT NULL,
            level INT DEFAULT 1,
            xp BIGINT DEFAULT 0,
            active BOOLEAN DEFAULT FALSE,
            candy_used INT DEFAULT 0,
            held_item VARCHAR(100),

            INDEX idx_owner (owner_uuid),
            FOREIGN KEY (owner_uuid) REFERENCES players(uuid) ON DELETE CASCADE
        )
        """);
}
```

---

## 15. Testing Protocol

### Manual Testing Checklist

Before committing any feature:

```markdown
## Feature: [Name]

### Basic Functionality
- [ ] Feature creates correctly
- [ ] Feature loads from database
- [ ] Feature saves to database
- [ ] Feature deletes correctly
- [ ] Cache works (second load is faster)

### GUI Testing
- [ ] Menu opens without errors
- [ ] All buttons are clickable
- [ ] Back button works
- [ ] Close button works
- [ ] Data displays correctly

### Permission Testing
- [ ] Works with permission
- [ ] Blocked without permission
- [ ] Admin bypass works (if applicable)

### Edge Cases
- [ ] Works with no existing data
- [ ] Works with max data
- [ ] Handles null gracefully
- [ ] Works after server restart

### Performance
- [ ] No TPS drop during normal use
- [ ] No memory leak (check after 10 min)
- [ ] Database queries are async

### Integration
- [ ] Doesn't break existing features
- [ ] Events fire correctly
- [ ] Other plugins can hook in
```

### Console Testing Commands

```
# Check for errors
/sbadmin debug on

# Test database
/sbadmin database test

# Check memory
/tps (or similar)

# Reload configs
/sbadmin reload
```

### Load Testing (Production)

```java
// Temporary code for load testing - REMOVE before production
public void loadTest() {
    for (int i = 0; i < 1000; i++) {
        UUID fake = UUID.randomUUID();
        createFeature(fake);
    }

    long start = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
        getFeature(someUuid);
    }
    long elapsed = System.currentTimeMillis() - start;
    plugin.log(Level.INFO, "100 lookups took " + elapsed + "ms");
}
```

---

## Quick Reference Card

### File Locations
| Type | Location |
|------|----------|
| Main class | `com.skyblock.SkyblockPlugin` |
| Managers | `com.skyblock.[feature].[Feature]Manager` |
| Commands | `com.skyblock.commands.[Feature]Command` |
| GUIs | `com.skyblock.gui.menus.[Feature]Menu` |
| Events | `com.skyblock.api.events.[Feature]Event` |
| Configs | `src/main/resources/[feature].yml` |

### Common Patterns
```java
// Get plugin instance
SkyblockPlugin plugin = SkyblockPlugin.getInstance();

// Get player data
SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);

// Check module
if (plugin.getModuleManager().isModuleEnabled("feature")) { }

// Send colored message
player.sendMessage(ColorUtils.colorize("&aSuccess!"));

// Open GUI
plugin.getGuiManager().openGUI(player, new SomeMenu(plugin));

// Async database
CompletableFuture.runAsync(() -> saveToDatabase(data));

// Log message
plugin.log(Level.INFO, "Message");
```

### PR Checklist
- [ ] Follows package structure
- [ ] Manager pattern implemented
- [ ] Database operations are async
- [ ] Cache implemented where needed
- [ ] Config file created
- [ ] Module check added
- [ ] Commands registered in plugin.yml
- [ ] Permissions defined
- [ ] JavaDoc on public methods
- [ ] Manual testing completed
- [ ] No hardcoded strings
- [ ] Error handling in place

---

*Last updated: Phase 1.5 - Version 1.5.0*
*This document should be updated with each phase release.*
