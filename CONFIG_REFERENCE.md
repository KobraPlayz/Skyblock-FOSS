# SkyblockFOSS Configuration Reference

## config.yml

Main configuration file for core settings.

```yaml
# Database Configuration
database:
  # Database type: 'mysql' or 'sqlite'
  type: sqlite

  # MySQL settings (when type: mysql)
  mysql:
    host: localhost
    port: 3306
    database: skyblock
    username: root
    password: ""
    useSSL: false
    poolSize: 10  # Connection pool size (10 recommended for 200+ players)

  # SQLite settings (when type: sqlite)
  sqlite:
    filename: data.db

# Cache Settings
cache:
  # Maximum number of players to keep in cache
  player-data-size: 500
  # Minutes before cached data expires (after last access)
  player-data-expire: 30

# Economy Settings
economy:
  # Starting coins for new players
  starting-coins: 500.0
  # Enable Vault integration
  vault-integration: true
  # Coin display format
  coin-symbol: ""
  coin-format: "#,##0.##"

# Profile Settings
profiles:
  # Maximum profiles per player
  max-profiles: 5
  # Default profile name
  default-name: "Default"
  # Allow profile deletion
  allow-deletion: true

# GUI Settings
gui:
  # Sound when opening menus
  open-sound: "BLOCK_CHEST_OPEN"
  # Sound when clicking items
  click-sound: "UI_BUTTON_CLICK"
  # Fill empty slots with glass panes
  fill-empty: true
  # Glass pane color (0-15)
  filler-data: 15

# Visual Effects
particles:
  # Enable particle effects
  enabled: true
  # Skill XP gain particles
  skill-xp: true
  # Level up particles
  level-up: true
  # Collection unlock particles
  collection-unlock: true

sounds:
  # Enable sound effects
  enabled: true
  # Skill XP gain sound
  skill-xp: true
  # Level up sound
  level-up: true

# Debug Settings
debug:
  # Enable debug logging
  enabled: false
  # Log database queries
  log-queries: false
```

## modules.yml

Module toggle configuration.

```yaml
# Module Configuration
# Enable/disable features individually

modules:
  # Skills System
  skills:
    enabled: true
    sub-modules:
      mining:
        enabled: true
      farming:
        enabled: true
      combat:
        enabled: true
      foraging:
        enabled: true
      fishing:
        enabled: true
      enchanting:
        enabled: true
      alchemy:
        enabled: true
      runecrafting:
        enabled: true
        coming-soon: true
      social:
        enabled: true
        coming-soon: true
      carpentry:
        enabled: true
        coming-soon: true
      taming:
        enabled: true
        coming-soon: true

  # Collections System
  collections:
    enabled: true

  # Profile System
  profiles:
    enabled: true

  # Economy System
  economy:
    enabled: true
    sub-modules:
      shops:
        enabled: false
        coming-soon: true
      trading:
        enabled: false
        coming-soon: true

  # Phase 2 Features
  pets:
    enabled: false
    coming-soon: true

  accessories:
    enabled: false
    coming-soon: true

  backpacks:
    enabled: false
    coming-soon: true

  banking:
    enabled: false
    coming-soon: true

  # Phase 3 Features
  minions:
    enabled: false
    coming-soon: true

  bazaar:
    enabled: false
    coming-soon: true

  auction-house:
    enabled: false
    coming-soon: true

  # Phase 4 Features
  dungeons:
    enabled: false
    coming-soon: true

  slayers:
    enabled: false
    coming-soon: true

  events:
    enabled: false
    coming-soon: true

  quests:
    enabled: false
    coming-soon: true

  fairy-souls:
    enabled: false
    coming-soon: true
```

## skills.yml

Skill definitions and XP requirements.

```yaml
# Skill XP Requirements
# Total XP needed to reach each level
xp-requirements:
  1: 50
  2: 175
  3: 375
  4: 675
  5: 1175
  # ... continues to level 60
  50: 55172425
  60: 111672425

# Skill Definitions
skills:
  mining:
    name: "Mining"
    color: "&b"
    material: DIAMOND_PICKAXE
    description: "Break ores and stone to level up"
    xp-sources:
      STONE: 1.0
      COBBLESTONE: 1.0
      COAL_ORE: 5.0
      DEEPSLATE_COAL_ORE: 6.0
      IRON_ORE: 10.0
      DEEPSLATE_IRON_ORE: 12.0
      GOLD_ORE: 15.0
      DEEPSLATE_GOLD_ORE: 18.0
      REDSTONE_ORE: 20.0
      DEEPSLATE_REDSTONE_ORE: 24.0
      LAPIS_ORE: 25.0
      DEEPSLATE_LAPIS_ORE: 30.0
      DIAMOND_ORE: 40.0
      DEEPSLATE_DIAMOND_ORE: 50.0
      EMERALD_ORE: 50.0
      DEEPSLATE_EMERALD_ORE: 60.0
      ANCIENT_DEBRIS: 500.0
    level-rewards:
      5:
        - type: STAT
          stat: DEFENSE
          amount: 1
      10:
        - type: STAT
          stat: DEFENSE
          amount: 2
      # ... continues

  farming:
    name: "Farming"
    color: "&a"
    material: GOLDEN_HOE
    description: "Harvest crops to level up"
    xp-sources:
      WHEAT: 4.0
      CARROTS: 4.0
      POTATOES: 4.0
      BEETROOTS: 4.0
      MELON: 4.0
      PUMPKIN: 4.5
      SUGAR_CANE: 4.0
      COCOA: 3.0
      CACTUS: 2.0
      NETHER_WART: 4.0
      SWEET_BERRY_BUSH: 3.0
    level-rewards:
      5:
        - type: STAT
          stat: HEALTH
          amount: 2
      # ... continues

  combat:
    name: "Combat"
    color: "&c"
    material: DIAMOND_SWORD
    description: "Kill mobs to level up"
    xp-sources:
      ZOMBIE: 6.0
      SKELETON: 6.0
      SPIDER: 5.0
      CREEPER: 8.0
      ENDERMAN: 20.0
      BLAZE: 15.0
      WITHER_SKELETON: 25.0
      WITCH: 10.0
      GUARDIAN: 12.0
      ELDER_GUARDIAN: 50.0
      PIGLIN_BRUTE: 30.0
      WARDEN: 250.0
      ENDER_DRAGON: 500.0
      WITHER: 500.0
    level-rewards:
      5:
        - type: STAT
          stat: CRIT_CHANCE
          amount: 1
      # ... continues

  # ... foraging, fishing, enchanting, alchemy, etc.
```

## collections.yml

Collection definitions and rewards.

```yaml
# Collection Categories
categories:
  farming:
    name: "Farming"
    material: WHEAT
    color: "&a"
  mining:
    name: "Mining"
    material: COBBLESTONE
    color: "&b"
  combat:
    name: "Combat"
    material: ROTTEN_FLESH
    color: "&c"
  foraging:
    name: "Foraging"
    material: OAK_LOG
    color: "&6"
  fishing:
    name: "Fishing"
    material: COD
    color: "&3"
  boss:
    name: "Boss"
    material: WITHER_SKELETON_SKULL
    color: "&5"

# Collection Definitions
collections:
  wheat:
    name: "Wheat"
    category: farming
    material: WHEAT
    tiers:
      1:
        requirement: 50
        rewards:
          - type: RECIPE
            item: "WHEAT_MINION_1"
      2:
        requirement: 100
        rewards:
          - type: RECIPE
            item: "ENCHANTED_BREAD"
      3:
        requirement: 250
        rewards:
          - type: RECIPE
            item: "WHEAT_MINION_2"
      # ... continues to tier 9

  cobblestone:
    name: "Cobblestone"
    category: mining
    material: COBBLESTONE
    tiers:
      1:
        requirement: 50
        rewards:
          - type: RECIPE
            item: "COBBLESTONE_MINION_1"
      2:
        requirement: 100
        rewards:
          - type: ITEM
            item: "TRAINING_WEIGHTS"
      # ... continues

  # ... more collections
```

## items.yml

Item definitions including weapons, armor, accessories.

```yaml
# Rarity Definitions
rarities:
  COMMON:
    name: "COMMON"
    color: "&f"
    bold: false
  UNCOMMON:
    name: "UNCOMMON"
    color: "&a"
    bold: false
  RARE:
    name: "RARE"
    color: "&9"
    bold: false
  EPIC:
    name: "EPIC"
    color: "&5"
    bold: false
  LEGENDARY:
    name: "LEGENDARY"
    color: "&6"
    bold: true
  MYTHIC:
    name: "MYTHIC"
    color: "&d"
    bold: true

# Stat Types
stat-types:
  DAMAGE:
    symbol: "❁"
    color: "&c"
  STRENGTH:
    symbol: "❁"
    color: "&c"
  CRIT_CHANCE:
    symbol: "☣"
    color: "&9"
  CRIT_DAMAGE:
    symbol: "☠"
    color: "&9"
  HEALTH:
    symbol: "❤"
    color: "&c"
  DEFENSE:
    symbol: "❈"
    color: "&a"
  SPEED:
    symbol: "✦"
    color: "&f"
  INTELLIGENCE:
    symbol: "✎"
    color: "&b"

# Item Definitions
items:
  # Weapons
  ASPECT_OF_THE_END:
    name: "Aspect of the End"
    material: DIAMOND_SWORD
    category: WEAPON
    rarity: RARE
    stats:
      DAMAGE: 100
      STRENGTH: 100
    ability:
      name: "Instant Transmission"
      trigger: RIGHT_CLICK
      description:
        - "&7Teleport &a8 blocks &7ahead of"
        - "&7you and gain &a+50 &f✦ Speed"
        - "&7for &a3 seconds&7."
      mana-cost: 50
      cooldown: 1

  HYPERION:
    name: "Hyperion"
    material: DIAMOND_SWORD
    category: WEAPON
    rarity: LEGENDARY
    stats:
      DAMAGE: 260
      STRENGTH: 150
      INTELLIGENCE: 350
    ability:
      name: "Wither Impact"
      trigger: RIGHT_CLICK
      description:
        - "&7Teleport &a10 blocks &7ahead"
        - "&7and create an explosion dealing"
        - "&c10,000 &7damage."
      mana-cost: 300
      cooldown: 0

  # Armor
  DIAMOND_HELMET_PERFECT:
    name: "Perfect Diamond Helmet"
    material: DIAMOND_HELMET
    category: HELMET
    rarity: EPIC
    stats:
      DEFENSE: 110
      HEALTH: 25

  # ... more items

# Reforge Definitions
reforges:
  weapon:
    SHARP:
      name: "Sharp"
      stats:
        DAMAGE: 10
        CRIT_CHANCE: 5
    SPICY:
      name: "Spicy"
      stats:
        DAMAGE: 5
        STRENGTH: 10
        CRIT_DAMAGE: 15
    LEGENDARY:
      name: "Legendary"
      stats:
        DAMAGE: 15
        STRENGTH: 15
        CRIT_CHANCE: 5
        CRIT_DAMAGE: 10
    # ... more reforges

  armor:
    PURE:
      name: "Pure"
      stats:
        HEALTH: 10
        DEFENSE: 10
        SPEED: 1
    FIERCE:
      name: "Fierce"
      stats:
        CRIT_CHANCE: 8
        CRIT_DAMAGE: 10
    # ... more reforges
```

## messages.yml

All customizable messages.

```yaml
# Message Prefix
prefix: "&6&lSkyblock &8» &r"

# General Messages
general:
  player-only: "&cThis command can only be used by players!"
  no-permission: "&cYou don't have permission to do this!"
  player-not-found: "&cPlayer &e{player} &cnot found!"
  invalid-number: "&c'{input}' is not a valid number!"
  feature-disabled: "&cThis feature is currently disabled!"
  coming-soon: "&eThis feature is coming soon!"

# Skill Messages
skills:
  xp-gain: "&b+{amount} {skill} XP"
  level-up: "&6&lSKILL LEVEL UP &e{skill} &7{old} → &e{new}"
  level-up-rewards: "&7You've unlocked new rewards! Check /skills."
  max-level: "&aYou've reached the maximum level in {skill}!"

# Collection Messages
collections:
  tier-unlock: "&6&lCOLLECTION UNLOCKED &e{collection} {tier}"
  reward-unlocked: "&aNew reward unlocked: &e{reward}"
  max-tier: "&aYou've maxed out {collection} collection!"

# Economy Messages
economy:
  balance: "&7Your purse: &6{amount} coins"
  received: "&a+{amount} coins"
  spent: "&c-{amount} coins"
  insufficient: "&cYou don't have enough coins! (need {amount})"

# Profile Messages
profiles:
  created: "&aProfile &e{name} &acreated!"
  deleted: "&cProfile &e{name} &cdeleted!"
  switched: "&aSwitched to profile &e{name}"
  max-profiles: "&cYou can only have {max} profiles!"
  already-exists: "&cA profile with that name already exists!"

# Admin Messages
admin:
  reload-success: "&aConfiguration reloaded successfully!"
  coins-given: "&aGave &6{amount} coins &ato &e{player}"
  coins-taken: "&aTook &6{amount} coins &afrom &e{player}"
  coins-set: "&aSet &e{player}'s &acoins to &6{amount}"
  skill-set: "&aSet &e{player}'s &a{skill} to level &e{level}"
  collection-set: "&aAdded &e{amount} &a{collection} to &e{player}"
  item-given: "&aGave &e{item} &ato &e{player}"
```

## File Locations

All configuration files are located in `plugins/SkyblockFOSS/`:

```
plugins/SkyblockFOSS/
├── config.yml       # Main configuration
├── modules.yml      # Module toggles
├── skills.yml       # Skill definitions
├── collections.yml  # Collection definitions
├── items.yml        # Item definitions
├── messages.yml     # Customizable messages
└── data.db          # SQLite database (if using SQLite)
```

## Reloading Configuration

Use `/sbadmin reload` to reload all configuration files without restarting the server.

Note: Database settings require a server restart to take effect.
