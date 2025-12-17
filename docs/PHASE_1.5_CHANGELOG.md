# Phase 1.5 Changelog

## Version 1.5.0-PHASE1.5 - Islands, Gardens & Worlds

### Major Features

#### Island System
- **Private Islands**: Each player gets their own private island
  - Default size: 160x160 blocks
  - Upgradable to 240x240 blocks
  - Floating void islands with starting platforms
- **Island Protection**: Full build/break protection for non-members
- **Island Settings**: Configurable via GUI
  - Public/Private toggle
  - PvP enable/disable
  - Mob spawning toggle
  - Animal spawning toggle
  - Visitor container access
- **Island Management**:
  - Set custom spawn points
  - Reset island (with confirmation)
  - View visitor statistics

#### Co-op System
- **Co-op Invites**: Invite up to 5 friends to share your island
- **Member Management**:
  - Invite/kick players
  - View all members
- **Voting System**: Kick votes require majority approval
- **Salvage Option**: Island owner can salvage the co-op

#### Garden System
- **Unlockable Area**: Available at SkyBlock Level 5
- **24 Garden Plots**: Each plot is 96x96 blocks
- **Plot Progression**:
  - Unlock plots with Copper or Garden Level
  - Clean plots with Compost
  - Apply presets or farm manually
- **Crop Upgrades**: Each upgrade grants +5 Farming Fortune
  - 10 crop types: Wheat, Carrot, Potato, Pumpkin, Melon, Cocoa Beans, Cactus, Sugar Cane, Nether Wart, Mushroom
  - Max level 10 per crop
- **Crop Milestones**: Track harvest progress and earn rewards
- **Garden Visitors**: NPCs that request items for Copper rewards
- **Garden Currencies**:
  - Copper: Earned from visitors, spent on upgrades
  - Compost: Used to clean plots
- **SkyMart Shop**: Purchase seeds, tools, and special items

#### World Management
- **Hub World**: Central spawn location
- **Dynamic World Creation**: Islands/Gardens created on demand
- **ASWM Support**: Optional Advanced Slime World Manager integration
- **Auto-Unload**: Inactive worlds unload after configurable time

#### Visitor System
- **Visit Public Islands**: `/visit <player>`
- **Social XP Rewards**: Earn Social skill XP when visited
- **Visitor Tracking**: Track total visitors and visit history

#### Furniture System
- **Placeable Items**: Decorative and functional furniture
- **15 Pieces Per Island**: Balanced limit
- **Furniture Types**:
  - Cosmetic: Tiki Torch, Decorative Skull, Flower Pot, etc.
  - Functional: Enhanced Chest, Enhanced Enchanting Table

### New Commands

| Command | Description |
|---------|-------------|
| `/island`, `/is` | Island management |
| `/visit <player>` | Visit other islands |
| `/hub` | Teleport to hub |
| `/coopadd`, `/coopkick`, `/coopview`, `/coopsalvage`, `/coopleave` | Co-op management |

### New Permissions

| Permission | Default |
|------------|---------|
| `skyblock.island` | true |
| `skyblock.visit` | true |
| `skyblock.hub` | true |
| `skyblock.coop` | true |
| `skyblock.garden` | true |

### New Configuration Files

- `islands.yml` - Island configuration
- `garden.yml` - Garden configuration
- `furniture.yml` - Furniture configuration
- `worlds.yml` - World management configuration

### Database Changes

Added 13 new tables for Phase 1.5 features. Database schema version updated from 1 to 2.

### GUI Additions

- **Island Settings Menu**: Configure island options
- **Garden Menu**: Main garden hub
- **Garden Upgrades (Desk)**: Upgrade crops
- **Garden Shop (SkyMart)**: Purchase items
- **Garden Milestones**: Track progress
- **Garden Visitors**: Complete requests
- **Garden Plots**: Manage plots
- **Updated SkyBlock Menu**: Added Island and Garden buttons

### API Events

- `IslandCreateEvent` - Fired when island is created
- `IslandTeleportEvent` - Fired on island teleport
- `GardenUnlockEvent` - Fired when garden is unlocked

### Technical Improvements

- **Caffeine Caching**: Island and Garden data cached for performance
- **Async Database Operations**: Non-blocking database calls
- **CompletableFuture API**: Modern async patterns
- **World Isolation**: Each island in separate world for performance

### Bug Fixes

- N/A (New features)

### Known Issues

- ASWM integration requires separate plugin installation
- Garden visitor spawning may need balancing

### Migration Notes

1. Backup your database before updating
2. New config files will be generated automatically
3. Existing player data is preserved
4. Phase 1 features continue to work unchanged
