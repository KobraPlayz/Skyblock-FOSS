# SkyblockFOSS Architecture

## Project Structure

```
src/main/java/com/skyblock/
├── SkyblockPlugin.java          # Main plugin entry point
├── api/
│   ├── SkyblockAPI.java         # Public API for external plugins
│   └── events/
│       ├── SkillLevelUpEvent.java
│       ├── CollectionUnlockEvent.java
│       ├── ProfileSwitchEvent.java
│       └── EconomyTransactionEvent.java
├── collections/
│   ├── Collection.java          # Collection data class
│   ├── CollectionCategory.java  # Collection category enum
│   ├── CollectionManager.java   # Collection management
│   ├── CollectionReward.java    # Reward data class
│   └── CollectionTier.java      # Tier data class
├── commands/
│   ├── AdminCommand.java        # Admin commands with tab complete
│   ├── CoinsCommand.java        # Balance check
│   ├── CollectionsCommand.java  # Collections menu
│   ├── ProfileCommand.java      # Profile management
│   ├── ShopCommand.java         # Shop access
│   ├── SkillsCommand.java       # Skills menu
│   └── SkyblockCommand.java     # Main menu
├── config/
│   └── ConfigManager.java       # Configuration handling
├── database/
│   └── DatabaseManager.java     # Database operations (HikariCP)
├── economy/
│   └── EconomyManager.java      # Coin management
├── gui/
│   ├── AbstractGUI.java         # Base GUI class
│   ├── GUIManager.java          # GUI handling
│   ├── menus/
│   │   ├── AdminMenu.java
│   │   ├── CollectionCategoryMenu.java
│   │   ├── CollectionsMenu.java
│   │   ├── ItemSpawnerMenu.java
│   │   ├── ProfileMenu.java
│   │   ├── SkillsMenu.java
│   │   └── SkyblockMenu.java
│   └── utils/
│       └── ItemBuilder.java     # Fluent item creation
├── items/
│   ├── CustomItem.java          # Custom item with NBT data
│   ├── ItemCategory.java        # Item category enum
│   ├── ItemManager.java         # Item registry and creation
│   ├── abilities/
│   │   ├── AbilityTrigger.java  # Ability trigger enum
│   │   └── ItemAbility.java     # Ability data class
│   ├── reforge/
│   │   ├── Reforge.java         # Reforge data class
│   │   └── ReforgeManager.java  # Reforge handling
│   ├── rarity/
│   │   └── Rarity.java          # Item rarity enum
│   └── stats/
│       ├── ItemStats.java       # Stats container
│       └── StatType.java        # Stat type enum
├── modules/
│   └── ModuleManager.java       # Feature toggle system
├── player/
│   ├── PlayerManager.java       # Player data management (cached)
│   ├── PlayerProfile.java       # Profile data
│   ├── PlayerStats.java         # Calculated player stats
│   └── SkyblockPlayer.java      # Player wrapper
├── skills/
│   ├── SkillManager.java        # Skill XP and levels
│   ├── SkillType.java           # Skill type enum
│   └── listeners/
│       ├── CombatListener.java
│       ├── FarmingListener.java
│       ├── FishingListener.java
│       ├── ForagingListener.java
│       └── MiningListener.java
└── utils/
    ├── ColorUtils.java          # Color code handling
    ├── NumberUtils.java         # Number formatting
    ├── ParticleUtils.java       # Particle effects
    ├── SoundUtils.java          # Sound effects
    └── TimeUtils.java           # Time formatting
```

## Core Components

### SkyblockPlugin (Main Class)

The central hub that initializes all managers and registers listeners/commands.

```java
public class SkyblockPlugin extends JavaPlugin {
    // Managers initialized in order of dependency
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ModuleManager moduleManager;
    private PlayerManager playerManager;
    private SkillManager skillManager;
    private CollectionManager collectionManager;
    private ItemManager itemManager;
    private ReforgeManager reforgeManager;
    private EconomyManager economyManager;
    private GUIManager guiManager;
    private SkyblockAPI api;
}
```

### Manager Pattern

All managers follow a consistent pattern:

1. **Initialization**: Called from main plugin, receives plugin reference
2. **Loading**: Loads data from config/database
3. **Runtime**: Provides methods for game logic
4. **Shutdown**: Saves data, closes connections

### Database Architecture

Uses HikariCP connection pooling for optimal performance:

```
┌─────────────────────────────────────────────────────────────┐
│                     DatabaseManager                          │
├─────────────────────────────────────────────────────────────┤
│  HikariDataSource (Connection Pool)                         │
│  ├── Pool Size: 10 (configurable)                           │
│  ├── Connection Timeout: 30s                                │
│  └── Idle Timeout: 10min                                    │
├─────────────────────────────────────────────────────────────┤
│  Tables (All phases pre-created):                           │
│  ├── Phase 1: players, profiles, skills, collections,       │
│  │            transactions                                   │
│  ├── Phase 2: pets, accessories, backpacks                  │
│  ├── Phase 3: minions, bazaar_orders, auctions,            │
│  │            auction_bids                                   │
│  └── Phase 4: dungeon_progress, dungeon_classes,           │
│               slayer_progress, quests, fairy_souls, events  │
└─────────────────────────────────────────────────────────────┘
```

### Caching Strategy

Player data uses Caffeine cache to minimize database hits:

```
┌────────────────────────────────────────────────────────────┐
│                     PlayerManager                           │
├────────────────────────────────────────────────────────────┤
│  Caffeine Cache<UUID, SkyblockPlayer>                      │
│  ├── Max Size: 500 entries (configurable)                  │
│  ├── Expiry: 30 minutes after access                       │
│  └── Removal Listener: Async save to database              │
├────────────────────────────────────────────────────────────┤
│  Load Flow:                                                 │
│  1. Player joins → Check cache                             │
│  2. Cache miss → Async DB load                             │
│  3. Cache hit → Return immediately                         │
│  4. Player quits → Mark for save (stays in cache)          │
└────────────────────────────────────────────────────────────┘
```

### Item System

Custom items use NBT tags for persistence:

```
┌────────────────────────────────────────────────────────────┐
│                     CustomItem                              │
├────────────────────────────────────────────────────────────┤
│  NBT Tags:                                                  │
│  ├── skyblock_item_id: String (e.g., "ASPECT_OF_THE_END")  │
│  ├── skyblock_rarity: String (e.g., "LEGENDARY")           │
│  ├── skyblock_reforge: String (e.g., "SHARP")              │
│  ├── skyblock_enchants: String (serialized map)            │
│  └── skyblock_uuid: String (unique instance ID)            │
├────────────────────────────────────────────────────────────┤
│  ItemStack Creation:                                        │
│  1. Get base material and meta                             │
│  2. Set display name with rarity color                     │
│  3. Build lore (stats, abilities, rarity line)             │
│  4. Apply NBT data                                         │
│  5. Return completed ItemStack                             │
└────────────────────────────────────────────────────────────┘
```

### Skill System

XP gain and leveling flow:

```
┌───────────────┐     ┌─────────────────┐     ┌──────────────┐
│ Block Break   │────▶│ MiningListener  │────▶│ SkillManager │
│ Entity Kill   │     │ CombatListener  │     │              │
│ Item Craft    │     │ etc.            │     │ addXp()      │
└───────────────┘     └─────────────────┘     └──────┬───────┘
                                                      │
                                                      ▼
                      ┌─────────────────┐     ┌──────────────┐
                      │ SkillLevelUp    │◀────│ checkLevelUp │
                      │ Event           │     │              │
                      └────────┬────────┘     └──────────────┘
                               │
                               ▼
                      ┌─────────────────┐
                      │ Apply Rewards   │
                      │ Update Stats    │
                      │ Send Message    │
                      └─────────────────┘
```

### GUI System

Abstract GUI framework for consistent menu handling:

```
┌─────────────────────────────────────────────────────────────┐
│                      AbstractGUI                             │
├─────────────────────────────────────────────────────────────┤
│  - title: String                                            │
│  - size: int (9, 18, 27, 36, 45, 54)                       │
│  - inventory: Inventory                                     │
│  - clickActions: Map<Integer, Consumer<Player>>             │
├─────────────────────────────────────────────────────────────┤
│  + setup(Player): void                                      │
│  + handleClick(InventoryClickEvent): void                   │
│  + setItem(slot, ItemStack, Consumer<Player>): void        │
│  + fillBorder(ItemStack): void                              │
└─────────────────────────────────────────────────────────────┘
        △
        │ extends
┌───────┴───────┬────────────────┬─────────────────┐
│ SkyblockMenu  │ SkillsMenu     │ CollectionsMenu │
│ ProfileMenu   │ AdminMenu      │ etc.            │
└───────────────┴────────────────┴─────────────────┘
```

### Module System

Feature toggles for gradual rollout:

```yaml
# modules.yml structure
modules:
  skills:
    enabled: true
    sub-modules:
      mining: { enabled: true }
      farming: { enabled: true }
  collections:
    enabled: true
  economy:
    enabled: true
    sub-modules:
      shops: { enabled: false, coming-soon: true }
  pets:
    enabled: false
    coming-soon: true
```

## Event Flow Examples

### Player Mines Block

```
1. BlockBreakEvent fired
2. MiningListener.onBlockBreak()
   ├── Check if enabled (ModuleManager)
   ├── Get SkyblockPlayer (PlayerManager cache)
   ├── Calculate XP from block type
   ├── Add skill XP (SkillManager)
   │   └── If level up → SkillLevelUpEvent
   └── Add collection (CollectionManager)
       └── If tier unlock → CollectionUnlockEvent
```

### Player Opens Menu

```
1. /skyblock command
2. SkyblockCommand.onCommand()
   ├── Check permission
   ├── Create SkyblockMenu
   └── GUIManager.openGUI()
       ├── Call menu.setup(player)
       ├── Register inventory in tracking map
       └── player.openInventory()

3. InventoryClickEvent
   └── GUIManager.onInventoryClick()
       ├── Find registered GUI
       └── Delegate to gui.handleClick()
```

## Extension Points

### Adding New Skills

1. Add to `SkillType.java` enum
2. Create new listener in `skills/listeners/`
3. Add XP sources to `skills.yml`
4. Register listener in `SkillManager`

### Adding New Items

1. Define item in `items.yml`
2. If new ability type, extend `ItemAbility`
3. Items auto-load via `ItemManager`

### Adding New Collections

1. Define in `collections.yml` with tiers
2. Link XP source in skill listener
3. Collections auto-load via `CollectionManager`

### Adding New GUIs

1. Extend `AbstractGUI`
2. Override `setup(Player)` to build menu
3. Use `setItem(slot, item, clickAction)` for interactivity

## Performance Considerations

1. **Async Database Operations**: All saves are async
2. **Caching**: Player data cached with Caffeine
3. **Lazy Loading**: Stats recalculated only when needed
4. **Event Priority**: Use appropriate priorities to avoid conflicts
5. **Connection Pooling**: HikariCP manages DB connections

## Thread Safety

- Main thread: All Bukkit API calls
- Async threads: Database operations, heavy calculations
- Cache: Thread-safe Caffeine implementation
- Synchronization: Use BukkitScheduler.runTask() to return to main thread
