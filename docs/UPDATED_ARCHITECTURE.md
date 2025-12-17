# Updated Architecture - Phase 1.5

This document describes the full architecture of SkyblockFOSS after Phase 1.5.

## Package Structure

```
com.skyblock/
├── SkyblockPlugin.java          # Main plugin class
├── api/
│   ├── SkyblockAPI.java         # Public API
│   └── events/
│       ├── IslandCreateEvent.java
│       ├── IslandTeleportEvent.java
│       └── GardenUnlockEvent.java
├── commands/
│   ├── SkyblockCommand.java
│   ├── SkillsCommand.java
│   ├── CollectionsCommand.java
│   ├── ProfileCommand.java
│   ├── AdminCommand.java
│   ├── CoinsCommand.java
│   ├── ShopCommand.java
│   ├── IslandCommand.java       # NEW Phase 1.5
│   ├── VisitCommand.java        # NEW Phase 1.5
│   ├── HubCommand.java          # NEW Phase 1.5
│   └── CoopCommand.java         # NEW Phase 1.5
├── config/
│   └── ConfigManager.java       # Handles all configs
├── coop/                        # NEW Phase 1.5
│   ├── CoopManager.java
│   ├── CoopInvite.java
│   └── KickVote.java
├── database/
│   └── DatabaseManager.java     # MySQL/SQLite handling
├── collections/
│   └── CollectionManager.java
├── economy/
│   └── EconomyManager.java
├── furniture/                   # NEW Phase 1.5
│   ├── Furniture.java
│   ├── FurnitureType.java
│   └── FurnitureManager.java
├── garden/                      # NEW Phase 1.5
│   ├── Garden.java
│   ├── GardenPlot.java
│   ├── CropType.java
│   ├── GardenVisitor.java
│   └── GardenManager.java
├── gui/
│   ├── AbstractGUI.java
│   ├── GUIManager.java
│   ├── utils/
│   │   └── ItemBuilder.java
│   └── menus/
│       ├── SkyblockMenu.java
│       ├── SkillsMenu.java
│       ├── CollectionsMenu.java
│       ├── ProfileMenu.java
│       ├── AdminMenu.java
│       ├── ItemSpawnerMenu.java
│       ├── CollectionCategoryMenu.java
│       ├── IslandSettingsMenu.java    # NEW Phase 1.5
│       ├── GardenMenu.java            # NEW Phase 1.5
│       ├── GardenUpgradesMenu.java    # NEW Phase 1.5
│       ├── GardenShopMenu.java        # NEW Phase 1.5
│       ├── GardenMilestonesMenu.java  # NEW Phase 1.5
│       ├── GardenVisitorsMenu.java    # NEW Phase 1.5
│       └── GardenPlotsMenu.java       # NEW Phase 1.5
├── island/                      # NEW Phase 1.5
│   ├── Island.java
│   ├── IslandRole.java
│   ├── IslandManager.java
│   └── IslandProtectionListener.java
├── items/
│   └── ItemManager.java
├── modules/
│   └── ModuleManager.java
├── player/
│   ├── PlayerManager.java
│   ├── SkyblockPlayer.java
│   └── Profile.java
├── skills/
│   └── SkillManager.java
├── utils/
│   ├── ColorUtils.java
│   └── NumberUtils.java
└── world/                       # NEW Phase 1.5
    └── WorldManager.java
```

## Manager Hierarchy

```
SkyblockPlugin
├── ConfigManager
│   ├── config.yml
│   ├── modules.yml
│   ├── skills.yml
│   ├── collections.yml
│   ├── items.yml
│   ├── messages.yml
│   ├── islands.yml      # NEW
│   ├── garden.yml       # NEW
│   ├── furniture.yml    # NEW
│   └── worlds.yml       # NEW
├── DatabaseManager
│   ├── HikariCP Pool
│   └── Schema Management
├── ModuleManager
├── PlayerManager
│   ├── SkyblockPlayer instances
│   └── Profile management
├── ItemManager
├── SkillManager
├── CollectionManager
├── EconomyManager
├── GUIManager
├── WorldManager         # NEW Phase 1.5
├── IslandManager        # NEW Phase 1.5
├── CoopManager          # NEW Phase 1.5
├── GardenManager        # NEW Phase 1.5
└── FurnitureManager     # NEW Phase 1.5
```

## Database Schema

### Phase 1 Tables (Existing)
- `players` - Player data
- `profiles` - Profile data
- `skills` - Skill progress
- `collections` - Collection progress

### Phase 1.5 Tables (New)
```sql
-- Island System
islands                  -- Core island data
island_members          -- Co-op members
island_settings         -- Island configuration
island_visitors         -- Visit tracking
island_bans             -- Banned players

-- Co-op System
coop_invites           -- Pending invitations
coop_kick_votes        -- Active kick votes

-- Garden System
gardens                -- Core garden data
garden_plots           -- Plot status
garden_crop_upgrades   -- Upgrade levels
garden_milestones      -- Harvest tracking
garden_visitors        -- NPC visitors

-- Furniture System
furniture              -- Placed furniture
```

## Data Flow

### Player Login
```
1. Player joins server
2. PlayerManager.loadPlayer()
3. Load from database (async)
4. Cache SkyblockPlayer
5. Ready to play
```

### Island Access
```
1. Player uses /island
2. IslandManager.teleportToIsland()
3. Check cache for island data
4. If not cached, load from DB
5. WorldManager.loadIslandWorld()
6. Teleport player
7. Fire IslandTeleportEvent
```

### Garden Access
```
1. Player opens Garden menu
2. GardenManager.getGarden()
3. Check cache
4. If not cached, load from DB
5. Display Garden GUI
6. Player interacts (upgrades, visitors, etc.)
7. Changes saved async
```

## Caching Strategy

### Caffeine Caches
```java
// Island cache
Cache<UUID, Island> islandCache = Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(30, TimeUnit.MINUTES)
    .build();

// Garden cache
Cache<UUID, Garden> gardenCache = Caffeine.newBuilder()
    .maximumSize(500)
    .expireAfterAccess(30, TimeUnit.MINUTES)
    .build();
```

### Cache Invalidation
- On player logout
- On explicit save
- On expiration (30 min)

## Event System

### Custom Events

```java
// Island created
IslandCreateEvent
├── getOwner(): UUID
├── getIsland(): Island
└── isCancelled(): boolean

// Island teleport
IslandTeleportEvent
├── getPlayer(): Player
├── getIsland(): Island
├── getFrom(): Location
├── getTo(): Location
└── isCancelled(): boolean

// Garden unlocked
GardenUnlockEvent
├── getPlayer(): Player
├── getGarden(): Garden
└── isCancelled(): boolean
```

### Listener Registration
```java
// In SkyblockPlugin.registerListeners()
getServer().getPluginManager().registerEvents(
    new IslandProtectionListener(this), this);
getServer().getPluginManager().registerEvents(
    furnitureManager, this);
getServer().getPluginManager().registerEvents(
    gardenManager, this);
```

## Configuration Files

### New in Phase 1.5

| File | Purpose |
|------|---------|
| `islands.yml` | Island settings, sizes, defaults |
| `garden.yml` | Garden settings, plots, visitors |
| `furniture.yml` | Furniture types and costs |
| `worlds.yml` | World management, ASWM, hub |

## Dependencies

### Required
- Paper/Spigot 1.20.4+
- Java 17+

### Optional
- Vault (economy integration)
- PlaceholderAPI (placeholders)
- Advanced Slime World Manager (better world handling)

### Libraries (Shaded)
- HikariCP (database pooling)
- Caffeine (caching)

## Performance Metrics

### Target Performance
- Island load: < 100ms (with ASWM)
- GUI open: < 50ms
- Database operations: async, non-blocking
- Memory per island: < 5MB (cached data)

### Monitoring Points
- Cache hit rates
- Database query times
- World load times
- Memory usage

## Future Extensibility

### Prepared for Phase 2
- Minion hooks in island system
- Pet storage in player data
- Accessory bag in profile

### API Stability
- Public API in `api` package
- Internal packages may change
- Events are stable contracts
