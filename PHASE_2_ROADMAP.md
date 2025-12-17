# Phase 2 Roadmap - SkyblockFOSS

## Overview

Phase 2 focuses on expanding the player systems with Pets, Accessories, Backpacks, and Banking. The database tables for these features are already created in Phase 1.

## Features to Implement

### 1. Pet System

**Description**: Pets provide stat bonuses and special abilities. Players can level pets and switch between them.

**Components**:
- `Pet.java` - Pet entity with levels, XP, tier, abilities
- `PetManager.java` - Pet management, leveling, abilities
- `PetType.java` - Enum of all pet types
- `PetAbility.java` - Pet ability definitions
- `PetMenu.java` - GUI for pet management
- `PetListener.java` - XP gain, ability triggers

**Database Table** (Already exists):
```sql
CREATE TABLE pets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    profile_id INT NOT NULL,
    pet_type VARCHAR(50) NOT NULL,
    tier VARCHAR(20) NOT NULL,
    xp BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    is_active BOOLEAN DEFAULT FALSE,
    candy_used INT DEFAULT 0
);
```

**Pet Types to Implement**:
- Combat Pets: Enderman, Zombie, Spider, Wolf, Ocelot, Tiger, Dragon
- Farming Pets: Bee, Rabbit, Elephant, Pig
- Mining Pets: Rock, Silverfish, Bat, Mithril Golem
- Fishing Pets: Dolphin, Squid, Flying Fish, Megalodon
- Foraging Pets: Monkey, Ocelot, Giraffe

**Pet Tiers**: Common, Uncommon, Rare, Epic, Legendary, Mythic

**Config Structure** (pets.yml):
```yaml
pets:
  ENDERMAN:
    name: "Enderman"
    head-texture: "..."
    category: combat
    stats:
      common:
        CRIT_DAMAGE: 0.5  # Per level
      legendary:
        CRIT_DAMAGE: 1.0
    abilities:
      common: []
      legendary:
        - name: "Teleport Savior"
          description: "Teleport away from fatal damage (5min cooldown)"
```

### 2. Accessory System

**Description**: Accessories go in the Accessory Bag and provide stat bonuses. Implement the full accessory bag progression.

**Components**:
- `Accessory.java` - Accessory item wrapper
- `AccessoryBag.java` - Player's accessory bag inventory
- `AccessoryManager.java` - Stat calculation, bag management
- `AccessoryBagMenu.java` - GUI for accessory bag

**Database Table** (Already exists):
```sql
CREATE TABLE accessories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    profile_id INT NOT NULL,
    accessory_type VARCHAR(100) NOT NULL,
    slot INT NOT NULL
);
```

**Accessory Bag Slots**:
- Redstone Collection unlocks (3, 6, 9... up to 81 slots)
- Each tier of Redstone collection adds 3 slots

**Key Accessories to Implement**:
- Talismans (basic stat boosts)
- Rings (medium stat boosts)
- Artifacts (powerful effects)
- Power Orbs (zone effects)

### 3. Backpack System

**Description**: Portable storage that can be accessed from inventory. Multiple backpack sizes.

**Components**:
- `Backpack.java` - Backpack container
- `BackpackManager.java` - Backpack handling, serialization
- `BackpackListener.java` - Right-click to open
- `BackpackMenu.java` - GUI for backpack contents

**Database Table** (Already exists):
```sql
CREATE TABLE backpacks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    profile_id INT NOT NULL,
    backpack_id VARCHAR(50) NOT NULL,
    size INT NOT NULL,
    contents LONGTEXT
);
```

**Backpack Sizes**:
- Small Backpack: 9 slots
- Medium Backpack: 18 slots
- Large Backpack: 27 slots
- Greater Backpack: 36 slots
- Jumbo Backpack: 45 slots

**Features**:
- Nested backpack prevention
- Backpack coloring/naming
- Quick transfer buttons

### 4. Banking System

**Description**: Secure coin storage with interest and co-op support.

**Components**:
- `Bank.java` - Bank account wrapper
- `BankManager.java` - Deposit/withdraw, interest calculation
- `BankMenu.java` - Banking GUI with transaction history
- `BankListener.java` - Interest ticks

**Features**:
- Personal bank per profile
- Deposit/withdraw coins
- Interest rate (configurable)
- Transaction history
- Bank upgrades for higher limits

**Config Structure** (economy.yml additions):
```yaml
banking:
  enabled: true
  interest-rate: 0.02  # 2% daily
  interest-interval: 86400  # Seconds (24 hours)
  default-limit: 50000000  # 50M
  upgrade-tiers:
    1: { limit: 100000000, cost: 1000000 }
    2: { limit: 250000000, cost: 5000000 }
    3: { limit: 500000000, cost: 25000000 }
```

### 5. Combined Stat Calculation

**Description**: Unify stat calculation from all sources.

**Update PlayerStats.java** to include:
- Base player stats
- Armor stats
- Weapon stats
- Pet bonus stats (when active)
- Accessory stats (from bag)
- Skill bonus stats
- Reforge stats
- Enchantment stats (Phase 3)

**Stat Priority/Order**:
1. Base stats (100 HP, etc.)
2. Equipment stats (additive)
3. Reforge stats (additive)
4. Pet stats (additive)
5. Accessory stats (additive)
6. Skill bonuses (additive)
7. Percentage modifiers (multiplicative)

## New Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/pets` | `skyblock.pets` | Open pet menu |
| `/accessorybag` or `/ab` | `skyblock.accessories` | Open accessory bag |
| `/bank` | `skyblock.bank` | Open bank menu |

## New Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/sbadmin pet <player> <pet> <tier> [level]` | `skyblock.admin.pets` | Give pet |
| `/sbadmin accessory <player> <accessory>` | `skyblock.admin.accessories` | Give accessory |
| `/sbadmin bank <player> <deposit/withdraw/set> <amount>` | `skyblock.admin.economy` | Manage bank |

## Estimated Implementation Order

1. **Pet System** (Most complex, core feature)
   - Pet data model
   - Pet manager with leveling
   - Pet GUI
   - Pet abilities framework
   - Individual pet implementations

2. **Accessory System**
   - Accessory data model
   - Accessory bag implementation
   - Stat calculation integration
   - Accessory bag GUI

3. **Backpack System**
   - Backpack data model
   - Serialization/deserialization
   - Backpack GUI
   - Item transfer handling

4. **Banking System**
   - Bank manager
   - Interest calculation
   - Bank GUI
   - Transaction logging

5. **Stat Calculation Unification**
   - Update PlayerStats
   - Integrate all stat sources
   - Performance optimization

## Extension Points from Phase 1

The following Phase 1 components are designed for Phase 2 extension:

- `PlayerStats.java` - Add `recalculateFromPets()`, `recalculateFromAccessories()`
- `CustomItem.java` - Already supports accessory category
- `ItemManager.java` - Can load accessory definitions from items.yml
- `ModuleManager.java` - Pets/accessories modules already defined

## Testing Checklist

- [ ] Pets can be obtained and equipped
- [ ] Pet XP gain works correctly
- [ ] Pet abilities trigger properly
- [ ] Accessory bag opens with correct slots
- [ ] Accessory stats are calculated
- [ ] Backpacks save/load correctly
- [ ] Bank deposits/withdrawals work
- [ ] Interest is applied correctly
- [ ] All stat sources combine properly
- [ ] Performance under load is acceptable
