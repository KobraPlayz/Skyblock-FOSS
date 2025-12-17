# Garden System Guide

The Garden is a separate farming-focused area unlocked at SkyBlock Level 5. It provides enhanced farming capabilities, crop upgrades, and unique rewards.

## Unlocking the Garden

### Requirements
- Reach **SkyBlock Level 5**
- Garden unlocks automatically

### First Access
Once unlocked, access via:
- `/garden` command
- SkyBlock Menu → The Garden
- Garden Portal (if configured)

## Garden Overview

The Garden features:
- **24 Plots**: Each 96x96 blocks
- **Crop Upgrades**: Boost Farming Fortune
- **Milestones**: Track harvest progress
- **Visitors**: NPCs requesting items
- **SkyMart**: Shop for seeds and tools
- **Unique Currencies**: Copper and Compost

## Garden Plots

### Plot Status

| Status | Icon | Description |
|--------|------|-------------|
| Locked | Gray | Not yet available |
| Unlocked | Yellow | Needs cleaning |
| Cleaned | Green | Ready for farming |
| Active | Cyan | Has preset applied |

### Unlocking Plots
Plots unlock via:
1. **Garden Level** - Higher levels unlock more plots
2. **Copper Purchase** - Buy unlock tokens from SkyMart (1,000 Copper)

### Cleaning Plots
Unlocked plots must be cleaned before use:
- Cost: **1,000 Compost**
- Clean via Garden Plots menu

### Plot Size
Each plot is **96x96 blocks** - plenty of space for large farms!

## Crop Upgrades

Access via the **Desk** in the Garden menu.

### How Upgrades Work
- Each crop has upgrade levels (max 10)
- Each level grants **+5 Farming Fortune**
- Max bonus: **+50 Farming Fortune per crop**

### Crop Types

| Crop | Material |
|------|----------|
| Wheat | Wheat |
| Carrot | Carrot |
| Potato | Potato |
| Pumpkin | Pumpkin |
| Melon | Melon Slice |
| Cocoa Beans | Cocoa Beans |
| Cactus | Cactus |
| Sugar Cane | Sugar Cane |
| Nether Wart | Nether Wart |
| Mushroom | Red Mushroom |

### Upgrade Costs
Costs increase per level. Typical progression:
- Level 1: 100 Copper
- Level 2: 200 Copper
- Level 3: 400 Copper
- Level 4: 800 Copper
- Level 5: 1,600 Copper
- ...and so on

## Crop Milestones

Track your farming progress for each crop type.

### How Milestones Work
- Harvest crops to increase your count
- Reach thresholds to unlock milestone levels
- Each milestone grants rewards

### Milestone Thresholds

| Level | Crops Required |
|-------|---------------|
| 1 | 100 |
| 2 | 500 |
| 3 | 1,000 |
| 4 | 2,500 |
| 5 | 5,000 |
| 6 | 10,000 |
| 7 | 25,000 |
| 8 | 50,000 |
| 9 | 100,000 |
| 10 | 250,000 |

### Milestone Rewards
Each milestone grants:
- **+5 Farming Fortune**
- **+100 Copper**
- Garden XP

## Garden Visitors

NPCs that visit your garden requesting items.

### How Visitors Work
1. Visitors spawn periodically (every 15 minutes)
2. They request specific items
3. Give them items to earn rewards
4. Visitors leave after time expires

### Visitor Rewards
- **Copper** - Main currency for upgrades
- **Garden XP** - Level up your garden
- Occasional special items

### Managing Visitors
- View active visitors in Garden menu
- Maximum 5 concurrent visitors
- Requests expire after 1 hour

## Garden Currencies

### Copper
**Primary currency** for the Garden.

**Earned from:**
- Completing visitor requests
- Milestone rewards
- Special events

**Spent on:**
- Crop upgrades
- SkyMart purchases
- Plot unlock tokens

### Compost
**Secondary currency** for cleaning.

**Earned from:**
- Composting organic materials
- Milestone rewards

**Spent on:**
- Cleaning plots (1,000 per plot)

## SkyMart Shop

The Garden's marketplace for purchasing items.

### Available Items

| Item | Cost | Amount |
|------|------|--------|
| Wheat Seeds | 10 Copper | 16 |
| Carrot Seeds | 15 Copper | 16 |
| Potato Seeds | 15 Copper | 16 |
| Pumpkin Seeds | 25 Copper | 8 |
| Melon Seeds | 25 Copper | 8 |
| Cocoa Beans | 30 Copper | 8 |
| Nether Wart | 50 Copper | 8 |
| Diamond Hoe | 500 Copper | 1 |
| Bone Meal | 5 Copper | 32 |
| Water Bucket | 50 Copper | 1 |
| Plot Unlock Token | 1,000 Copper | 1 |
| Garden XP Boost | 250 Copper | 500 XP |

## Garden Level

Your Garden has its own level system.

### Leveling Up
- Earn XP from harvesting
- Earn XP from visitor completions
- Earn XP from milestones

### Level Benefits
Higher Garden level unlocks:
- More plots
- Better visitor rewards
- Achievement bonuses

## Farming Fortune

**Farming Fortune** is the key stat in the Garden.

### What It Does
- Increases crop yields
- More drops per harvest
- Stacks with tool enchantments

### Sources of Farming Fortune
1. **Crop Upgrades**: +5 per level (max +50 each)
2. **Milestones**: +5 per milestone level
3. **Farming Skill**: Based on skill level
4. **Equipment**: Hoes and accessories

### Total Potential
- 10 crops × 10 levels × 5 = +500 from upgrades
- 10 crops × milestones = hundreds more
- Massive farming potential!

## Tips for Garden Success

### Getting Started
1. Focus on one crop first
2. Complete visitor requests for Copper
3. Upgrade your best crop
4. Expand to more crops

### Efficient Farming
1. Use Farming Fortune tools
2. Create large farms on cleaned plots
3. Set up auto-harvest systems (if available)
4. Track milestones for bonuses

### Copper Farming
1. Always complete visitor requests
2. Higher-level visitors pay more
3. Fast item collection = more completions

### Progression Path
1. Unlock Garden at Level 5
2. Clean your first plot
3. Start farming and completing visitors
4. Upgrade crops with Copper
5. Unlock more plots
6. Reach milestone rewards
7. Maximize Farming Fortune!

## Technical Details

### Data Storage
Garden data stored in MySQL/MariaDB:
- `gardens`: Core garden data
- `garden_plots`: Plot status
- `garden_crop_upgrades`: Upgrade levels
- `garden_milestones`: Harvest counts
- `garden_visitors`: Active visitors

### Caching
Garden data cached with Caffeine:
- 500 gardens cached
- 30-minute expiration

## Troubleshooting

### Can't Access Garden
- Check SkyBlock Level (need 5)
- Verify garden module enabled

### Visitors Not Spawning
- Wait for spawn timer (15 min)
- Check max visitor limit

### Upgrades Not Working
- Verify Copper balance
- Check max level not reached

### Plot Won't Clean
- Need 1,000 Compost
- Plot must be Unlocked status
