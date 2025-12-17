# Phase 1.5 Setup Guide

This guide covers the setup and configuration of Phase 1.5 features including Islands, Gardens, Co-op, and World Management.

## Prerequisites

- SkyblockFOSS Phase 1 installed and configured
- MySQL/MariaDB database (recommended) or SQLite
- Paper/Spigot 1.20.4+
- Optional: Advanced Slime World Manager (ASWM) for improved performance

## Installation

1. **Replace the JAR file** with the new Phase 1.5 version
2. **Stop your server** before updating
3. **Backup your database** and world files
4. Start the server - new configuration files will be generated

## New Configuration Files

Phase 1.5 adds the following configuration files:

### islands.yml
```yaml
# Island settings
island:
  default-size: 160          # Default island size (160x160)
  max-size: 240              # Maximum island size after upgrades
  spawn-y: 100               # Y level for island spawn
  border-buffer: 16          # Buffer between islands

settings:
  default-public: false      # Islands private by default
  default-pvp: false         # PvP disabled by default
  max-members: 5             # Max co-op members
  visitor-cooldown: 300      # Seconds between visits for XP
```

### garden.yml
```yaml
# Garden settings
garden:
  unlock-level: 5            # SkyBlock level required
  plot-size: 96              # Each plot is 96x96
  total-plots: 24            # Total plots available

visitors:
  spawn-interval: 900        # Seconds between visitor spawns
  max-active: 5              # Max concurrent visitors
  expiration: 3600           # Visitor request expiration (1 hour)
```

### furniture.yml
```yaml
# Furniture settings
furniture:
  max-per-island: 15         # Max furniture pieces per island

types:
  TIKI_TORCH:
    enabled: true
    cost: 1000
  DECORATIVE_SKULL:
    enabled: true
    cost: 2500
  # ... more types
```

### worlds.yml
```yaml
# World management settings
worlds:
  hub:
    name: "hub"
    spawn:
      x: 0.5
      y: 100
      z: 0.5

  use-slime-world-manager: false  # Enable if ASWM is installed
  auto-unload-minutes: 30         # Unload inactive islands after 30 min
```

## Database Migration

Phase 1.5 automatically creates new tables on first run:

- `islands` - Island data
- `island_members` - Co-op members
- `island_settings` - Island configuration
- `island_visitors` - Visitor tracking
- `island_bans` - Banned players
- `coop_invites` - Pending invitations
- `coop_kick_votes` - Active kick votes
- `gardens` - Garden data
- `garden_plots` - Plot status
- `garden_crop_upgrades` - Crop upgrade levels
- `garden_milestones` - Harvest milestones
- `garden_visitors` - Garden visitor NPCs
- `furniture` - Placed furniture

## New Commands

### Island Commands
| Command | Description |
|---------|-------------|
| `/island` or `/is` | Teleport to your island |
| `/island create` | Create a new island |
| `/island settings` | Open island settings GUI |
| `/island sethome` | Set island spawn point |
| `/island reset` | Reset your island (requires confirmation) |
| `/island invite <player>` | Invite player to co-op |
| `/island kick <player>` | Start kick vote |
| `/island ban <player>` | Ban player from island |
| `/island unban <player>` | Unban player |
| `/island visitors` | View visitor stats |
| `/island public` | Toggle public island |
| `/island pvp` | Toggle PvP |

### Visit Commands
| Command | Description |
|---------|-------------|
| `/visit <player>` | Visit player's island |
| `/hub` | Teleport to hub |

### Co-op Commands
| Command | Description |
|---------|-------------|
| `/coopadd <player>` | Invite to co-op |
| `/coopkick <player>` | Start kick vote |
| `/coopview` | View co-op members |
| `/coopsalvage` | Salvage co-op (owner only) |
| `/coopleave` | Leave co-op |

## New Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `skyblock.island` | Access island commands | true |
| `skyblock.visit` | Visit other islands | true |
| `skyblock.hub` | Teleport to hub | true |
| `skyblock.coop` | Access co-op features | true |
| `skyblock.garden` | Access garden features | true |

## World Setup

### Hub World
Create a world named `hub` (or configure in worlds.yml) with your hub build. Set the spawn location in the configuration.

### Island Worlds
Islands are created dynamically in separate worlds. With ASWM, these are stored as slime files. Without ASWM, standard Bukkit worlds are used.

### Garden World
Gardens are per-player and created on first unlock. They function similarly to island worlds.

## Performance Optimization

### With ASWM (Recommended)
1. Install Advanced Slime World Manager
2. Set `use-slime-world-manager: true` in worlds.yml
3. Islands load instantly and use minimal memory

### Without ASWM
1. Configure `auto-unload-minutes` to unload inactive islands
2. Consider using async world loading
3. Monitor memory usage

## Troubleshooting

### Islands not creating
- Check console for errors
- Verify database connection
- Ensure world permissions are correct

### Garden not unlocking
- Player must reach SkyBlock Level 5
- Check if garden module is enabled

### Co-op issues
- Verify island exists before inviting
- Check max member limits

## Support

For issues, please open a GitHub issue with:
- Server version
- Full error logs
- Steps to reproduce
