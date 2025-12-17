# API Updates - Phase 1.5

This document covers the new API features and events added in Phase 1.5.

## New Events

### IslandCreateEvent

Fired when a new island is created for a player.

```java
import com.skyblock.api.events.IslandCreateEvent;

@EventHandler
public void onIslandCreate(IslandCreateEvent event) {
    UUID owner = event.getOwner();
    Island island = event.getIsland();

    // Cancel to prevent island creation
    if (someCondition) {
        event.setCancelled(true);
    }

    // Log creation
    getLogger().info("Island created for " + owner);
}
```

**Event Properties:**
| Method | Return Type | Description |
|--------|-------------|-------------|
| `getOwner()` | UUID | Island owner's UUID |
| `getIsland()` | Island | The created island |
| `isCancelled()` | boolean | Whether event is cancelled |
| `setCancelled(boolean)` | void | Cancel the event |

### IslandTeleportEvent

Fired when a player teleports to an island.

```java
import com.skyblock.api.events.IslandTeleportEvent;

@EventHandler
public void onIslandTeleport(IslandTeleportEvent event) {
    Player player = event.getPlayer();
    Island island = event.getIsland();
    Location from = event.getFrom();
    Location to = event.getTo();

    // Modify destination
    event.setTo(modifiedLocation);

    // Cancel teleport
    if (playerIsBanned(player, island)) {
        event.setCancelled(true);
        player.sendMessage("You are banned from this island!");
    }
}
```

**Event Properties:**
| Method | Return Type | Description |
|--------|-------------|-------------|
| `getPlayer()` | Player | Teleporting player |
| `getIsland()` | Island | Target island |
| `getFrom()` | Location | Origin location |
| `getTo()` | Location | Destination location |
| `setTo(Location)` | void | Modify destination |
| `isCancelled()` | boolean | Whether cancelled |
| `setCancelled(boolean)` | void | Cancel teleport |

### GardenUnlockEvent

Fired when a player unlocks their garden.

```java
import com.skyblock.api.events.GardenUnlockEvent;

@EventHandler
public void onGardenUnlock(GardenUnlockEvent event) {
    Player player = event.getPlayer();
    Garden garden = event.getGarden();

    // Give welcome bonus
    garden.setCopper(garden.getCopper() + 100);
    player.sendMessage("Garden unlocked! +100 Copper bonus!");

    // Cancel to prevent unlock
    if (!meetsCustomRequirements(player)) {
        event.setCancelled(true);
    }
}
```

**Event Properties:**
| Method | Return Type | Description |
|--------|-------------|-------------|
| `getPlayer()` | Player | Player unlocking garden |
| `getGarden()` | Garden | The unlocked garden |
| `isCancelled()` | boolean | Whether cancelled |
| `setCancelled(boolean)` | void | Cancel unlock |

## New Manager Access

Access new managers through SkyblockAPI:

```java
SkyblockPlugin plugin = SkyblockPlugin.getInstance();

// Island Manager
IslandManager islandManager = plugin.getIslandManager();

// Co-op Manager
CoopManager coopManager = plugin.getCoopManager();

// Garden Manager
GardenManager gardenManager = plugin.getGardenManager();

// World Manager
WorldManager worldManager = plugin.getWorldManager();

// Furniture Manager
FurnitureManager furnitureManager = plugin.getFurnitureManager();
```

## Island API

### Getting Island Data

```java
IslandManager manager = plugin.getIslandManager();

// Get by owner UUID
Island island = manager.getIsland(playerUuid);

// Check if exists
if (manager.hasIsland(playerUuid)) {
    // ...
}
```

### Creating Islands

```java
// Async creation
manager.createIsland(playerUuid).thenAccept(island -> {
    // Island created
    System.out.println("Created island: " + island.getId());
});
```

### Teleporting to Islands

```java
// Teleport player to their island
manager.teleportToIsland(player, player.getUniqueId());

// Teleport player to another's island (visiting)
manager.visitIsland(player, targetPlayerUuid);
```

### Island Settings

```java
Island island = manager.getIsland(playerUuid);

// Get setting
boolean isPublic = island.getSetting("public", false);

// Set setting
island.setSetting("pvp", true);

// Save changes
manager.saveIsland(island);
```

### Island Members

```java
Island island = manager.getIsland(ownerUuid);

// Get all members
Set<UUID> members = island.getMembers();

// Check membership
if (island.isMember(playerUuid)) {
    // Player is a member
}

// Add member
island.addMember(playerUuid);
manager.saveIsland(island);
```

## Co-op API

### Managing Invites

```java
CoopManager coop = plugin.getCoopManager();

// Send invite
coop.sendInvite(ownerUuid, targetUuid);

// Accept invite
coop.acceptInvite(targetUuid, ownerUuid);

// Deny invite
coop.denyInvite(targetUuid, ownerUuid);
```

### Kick Voting

```java
// Start a kick vote
coop.startKickVote(initiatorUuid, targetUuid);

// Cast vote
coop.castKickVote(voterUuid, targetUuid, true); // true = kick
```

## Garden API

### Getting Garden Data

```java
GardenManager manager = plugin.getGardenManager();

// Get garden
Garden garden = manager.getGarden(playerUuid);

// Check if unlocked
if (garden != null) {
    // Garden is unlocked
}
```

### Garden Currencies

```java
Garden garden = manager.getGarden(playerUuid);

// Copper
long copper = garden.getCopper();
garden.setCopper(copper + 100);

// Compost
long compost = garden.getCompost();
garden.setCompost(compost + 50);

manager.saveGarden(garden);
```

### Crop Upgrades

```java
Garden garden = manager.getGarden(playerUuid);

// Get upgrade level
int level = garden.getCropUpgradeLevel(CropType.WHEAT);

// Set upgrade level
garden.setCropUpgradeLevel(CropType.WHEAT, level + 1);

manager.saveGarden(garden);
```

### Milestones

```java
Garden garden = manager.getGarden(playerUuid);

// Get milestone
Garden.CropMilestone milestone = garden.getMilestone(CropType.WHEAT);
long harvested = milestone.getTotalHarvested();
int milestoneLevel = milestone.getMilestoneLevel();

// Update milestone
milestone.setTotalHarvested(harvested + 100);
manager.saveGarden(garden);
```

### Garden Visitors

```java
Garden garden = manager.getGarden(playerUuid);

// Get active visitors
List<GardenVisitor> visitors = garden.getActiveVisitors();

// Spawn a visitor
manager.spawnVisitor(garden);

// Complete visitor request
manager.completeVisitor(garden, visitor);
```

## World API

### World Management

```java
WorldManager manager = plugin.getWorldManager();

// Check if island world exists
boolean exists = manager.islandWorldExists(playerUuid);

// Load island world
CompletableFuture<World> worldFuture = manager.loadIslandWorld(playerUuid);
worldFuture.thenAccept(world -> {
    // World loaded
});

// Unload island world
manager.unloadIslandWorld(playerUuid);
```

### Teleportation

```java
WorldManager manager = plugin.getWorldManager();

// Teleport to hub
manager.teleportToHub(player);

// Teleport to island
manager.teleportToIsland(player, ownerUuid);
```

## Furniture API

### Placing Furniture

```java
FurnitureManager manager = plugin.getFurnitureManager();

// Place furniture
Furniture furniture = manager.placeFurniture(
    islandId,
    FurnitureType.TIKI_TORCH,
    location
);
```

### Removing Furniture

```java
// Remove by ID
manager.removeFurniture(furnitureId);

// Remove at location
manager.removeFurnitureAt(location);
```

### Getting Furniture

```java
// Get all on island
List<Furniture> furniture = manager.getIslandFurniture(islandId);

// Get at location
Furniture atLocation = manager.getFurnitureAt(location);
```

## Example Plugin Integration

```java
public class MyAddon extends JavaPlugin {

    @Override
    public void onEnable() {
        // Wait for SkyblockFOSS to enable
        if (getServer().getPluginManager().getPlugin("SkyblockFOSS") != null) {
            registerEvents();
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onIslandCreate(IslandCreateEvent event) {
                // Custom island creation logic
                Island island = event.getIsland();

                // Set custom starting bonus
                SkyblockPlugin.getInstance()
                    .getEconomyManager()
                    .addCoins(event.getOwner(), 1000);
            }

            @EventHandler
            public void onGardenUnlock(GardenUnlockEvent event) {
                // Give garden starter kit
                Player player = event.getPlayer();
                player.getInventory().addItem(/* seeds */);
            }

        }, this);
    }
}
```

## Compatibility Notes

### Breaking Changes
None - Phase 1.5 is fully backwards compatible with Phase 1.

### Deprecated
Nothing deprecated in this release.

### Future API
The following may be added in Phase 2:
- Minion placement events
- Pet level events
- Accessory equip events
