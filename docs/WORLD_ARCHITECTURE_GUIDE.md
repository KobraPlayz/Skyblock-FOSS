# World Architecture Guide

This guide explains the world management system in SkyblockFOSS Phase 1.5, including how islands, gardens, and shared worlds are structured.

## Architecture Overview

SkyblockFOSS uses a multi-world architecture where different areas are separate Minecraft worlds:

```
┌─────────────────────────────────────────────────────────┐
│                    World Architecture                    │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │                   HUB WORLD                       │   │
│  │   - Central spawn point                           │   │
│  │   - Shared by all players                         │   │
│  │   - Contains NPCs, portals, shops                 │   │
│  └──────────────────────────────────────────────────┘   │
│                          │                               │
│                          ▼                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │              ISLAND WORLDS                        │   │
│  │                                                   │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐            │   │
│  │  │Player A │ │Player B │ │Player C │ ...        │   │
│  │  │ Island  │ │ Island  │ │ Island  │            │   │
│  │  └─────────┘ └─────────┘ └─────────┘            │   │
│  │                                                   │   │
│  │   - One world per island                          │   │
│  │   - Dynamically created/loaded                    │   │
│  │   - Auto-unloaded when inactive                   │   │
│  └──────────────────────────────────────────────────┘   │
│                          │                               │
│                          ▼                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │              GARDEN WORLDS                        │   │
│  │                                                   │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐            │   │
│  │  │Player A │ │Player B │ │Player C │ ...        │   │
│  │  │ Garden  │ │ Garden  │ │ Garden  │            │   │
│  │  └─────────┘ └─────────┘ └─────────┘            │   │
│  │                                                   │   │
│  │   - Per-player garden worlds                      │   │
│  │   - 24 plots per garden                           │   │
│  │   - Separate from island                          │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

## World Types

### Hub World
The central shared world where all players spawn.

**Configuration** (`worlds.yml`):
```yaml
worlds:
  hub:
    name: "hub"
    spawn:
      x: 0.5
      y: 100
      z: 0.5
      yaw: 0
      pitch: 0
```

**Characteristics:**
- Persistent - always loaded
- Shared by all players
- Contains portals, NPCs, shops
- No building by players (protected)

### Island Worlds
Private worlds for each player's island.

**World Naming Convention:**
```
island_<uuid>
```
Example: `island_123e4567-e89b-12d3-a456-426614174000`

**Characteristics:**
- Created on first `/island` command
- One world per island owner
- Co-op members share the same world
- Dynamically loaded/unloaded

### Garden Worlds
Farming-focused worlds for each player.

**World Naming Convention:**
```
garden_<uuid>
```

**Characteristics:**
- Created when Garden unlocks (Level 5)
- Contains 24 farming plots
- Separate from main island
- Dynamically loaded/unloaded

## WorldManager Class

The `WorldManager` class handles all world operations.

### Key Methods

```java
// Create a new island world
CompletableFuture<World> createIslandWorld(UUID ownerUuid)

// Load an existing island world
CompletableFuture<World> loadIslandWorld(UUID ownerUuid)

// Unload an inactive island world
void unloadIslandWorld(UUID ownerUuid)

// Teleport player to hub
void teleportToHub(Player player)

// Teleport player to their island
void teleportToIsland(Player player, UUID islandOwner)

// Check if world exists
boolean islandWorldExists(UUID ownerUuid)
```

### World Creation Process

1. Generate unique world name
2. Create WorldCreator with settings:
   - VOID generator (empty world)
   - No structures
   - Custom biome
3. Create world
4. Generate starting island structure
5. Set spawn point
6. Save to database

## Slime World Manager Integration

For servers with Advanced Slime World Manager (ASWM), worlds use the slime format.

### Benefits of ASWM
- **Instant Loading**: Worlds load in milliseconds
- **Lower Memory**: Compressed world storage
- **Better Performance**: Optimized chunk handling
- **Portable**: Single file per world

### Configuration
```yaml
worlds:
  use-slime-world-manager: true
```

### Without ASWM
Standard Bukkit world management is used:
- Worlds stored as folder structures
- Standard chunk loading
- Works on all servers

## Auto-Unload System

To manage memory, inactive worlds are automatically unloaded.

### Configuration
```yaml
worlds:
  auto-unload-minutes: 30
```

### How It Works
1. Task runs every 5 minutes
2. Checks last access time for each world
3. Unloads worlds inactive longer than threshold
4. Saves world data before unloading

### Exemptions
- Hub world never unloads
- Worlds with online players never unload

## Teleportation System

### Hub Teleport
```java
worldManager.teleportToHub(player);
```
Always teleports to configured hub spawn.

### Island Teleport
```java
worldManager.teleportToIsland(player, ownerUuid);
```
Process:
1. Load island world if not loaded
2. Get island spawn point
3. Teleport player
4. Fire IslandTeleportEvent

### Garden Teleport
```java
gardenManager.teleportToGarden(player);
```
Process:
1. Verify garden unlocked
2. Load garden world if needed
3. Teleport to garden spawn

## World Protection

### Hub Protection
- No block breaking
- No block placing
- No PvP (configurable)
- NPC interaction allowed

### Island Protection
- Only members can build
- Settings control visitor permissions
- PvP toggle per island
- Mob spawning toggle

### Garden Protection
- Owner only access
- No visitors
- Protected environment

## Performance Considerations

### Memory Management
- Cache island data, not worlds
- Unload inactive worlds
- Use ASWM if possible

### Loading Optimization
- Async world loading
- Pre-load chunks at spawn
- Lazy loading for distant chunks

### Database
- Async save operations
- Batch updates
- Connection pooling

## Configuration Reference

### worlds.yml
```yaml
worlds:
  # Hub world configuration
  hub:
    name: "hub"
    spawn:
      x: 0.5
      y: 100
      z: 0.5
      yaw: 0
      pitch: 0

  # Island world settings
  island:
    default-size: 160
    max-size: 240
    spawn-y: 100
    generator: "VOID"

  # Garden world settings
  garden:
    spawn-y: 100
    plot-size: 96

  # Performance settings
  use-slime-world-manager: false
  auto-unload-minutes: 30
  chunk-preload-radius: 3
```

## Troubleshooting

### World Won't Create
- Check disk space
- Verify world folder permissions
- Check console for errors

### World Won't Load
- Verify world files exist
- Check ASWM installation (if enabled)
- Look for corruption errors

### Teleport Failures
- Ensure world is loaded
- Check spawn point is safe
- Verify player permissions

### Memory Issues
- Reduce auto-unload time
- Enable ASWM
- Monitor with `/timings`
