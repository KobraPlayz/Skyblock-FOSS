# SkyblockFOSS API Documentation

## Overview

SkyblockFOSS provides a public API for other plugins to interact with the Skyblock systems. The API is accessed through the `SkyblockAPI` class.

## Getting the API Instance

```java
import com.skyblock.api.SkyblockAPI;
import com.skyblock.SkyblockPlugin;

public class YourPlugin extends JavaPlugin {

    private SkyblockAPI skyblockAPI;

    @Override
    public void onEnable() {
        // Get the SkyblockFOSS plugin
        Plugin skyblock = getServer().getPluginManager().getPlugin("SkyblockFOSS");

        if (skyblock != null && skyblock.isEnabled()) {
            SkyblockPlugin sbPlugin = (SkyblockPlugin) skyblock;
            skyblockAPI = sbPlugin.getAPI();
        }
    }
}
```

## API Methods

### Player Data

```java
// Get a player's Skyblock data
SkyblockPlayer getPlayer(Player player);
SkyblockPlayer getPlayer(UUID uuid);

// Check if player data is loaded
boolean isPlayerLoaded(UUID uuid);
```

### Skills

```java
// Get skill level
int getSkillLevel(Player player, String skillName);
int getSkillLevel(Player player, SkillType skill);

// Get skill XP
double getSkillXP(Player player, String skillName);
double getSkillXP(Player player, SkillType skill);

// Add skill XP
void addSkillXP(Player player, String skillName, double amount);
void addSkillXP(Player player, SkillType skill, double amount);

// Set skill level (admin)
void setSkillLevel(Player player, String skillName, int level);
```

### Collections

```java
// Get collection amount
long getCollectionAmount(Player player, String collectionId);

// Get collection tier (0 = not unlocked)
int getCollectionTier(Player player, String collectionId);

// Add to collection
void addCollection(Player player, String collectionId, long amount);

// Check if tier is unlocked
boolean hasCollectionTier(Player player, String collectionId, int tier);
```

### Economy

```java
// Get player's purse balance
double getCoins(Player player);

// Add coins
void addCoins(Player player, double amount);

// Remove coins (returns false if insufficient)
boolean removeCoins(Player player, double amount);

// Set coins
void setCoins(Player player, double amount);

// Get bank balance (Phase 2)
double getBankBalance(Player player);
```

### Items

```java
// Create a custom item
ItemStack createItem(String itemId);
ItemStack createItem(String itemId, int amount);

// Give item to player
void giveItem(Player player, String itemId);
void giveItem(Player player, String itemId, int amount);

// Check if ItemStack is a custom item
boolean isCustomItem(ItemStack item);

// Get custom item ID from ItemStack
String getItemId(ItemStack item);

// Get all registered item IDs
Set<String> getItemIds();
```

### Stats

```java
// Get player's calculated stats
PlayerStats getPlayerStats(Player player);

// Get specific stat value
double getStat(Player player, StatType stat);

// Recalculate stats (after equipment change)
void recalculateStats(Player player);
```

### Profiles

```java
// Get active profile
PlayerProfile getActiveProfile(Player player);

// Get all profiles for player
List<PlayerProfile> getProfiles(Player player);

// Get profile count
int getProfileCount(Player player);

// Switch profile
boolean switchProfile(Player player, String profileName);
```

## Custom Events

### SkillLevelUpEvent

Fired when a player levels up a skill.

```java
import com.skyblock.api.events.SkillLevelUpEvent;

@EventHandler
public void onSkillLevelUp(SkillLevelUpEvent event) {
    Player player = event.getPlayer();
    SkillType skill = event.getSkill();
    int oldLevel = event.getOldLevel();
    int newLevel = event.getNewLevel();

    // Custom logic here
}
```

### CollectionUnlockEvent

Fired when a player unlocks a new collection tier.

```java
import com.skyblock.api.events.CollectionUnlockEvent;

@EventHandler
public void onCollectionUnlock(CollectionUnlockEvent event) {
    Player player = event.getPlayer();
    String collectionId = event.getCollectionId();
    int tier = event.getTier();

    // Custom logic here
}
```

### ProfileSwitchEvent

Fired when a player switches profiles.

```java
import com.skyblock.api.events.ProfileSwitchEvent;

@EventHandler
public void onProfileSwitch(ProfileSwitchEvent event) {
    Player player = event.getPlayer();
    PlayerProfile oldProfile = event.getOldProfile();
    PlayerProfile newProfile = event.getNewProfile();

    // This event is cancellable
    if (someCondition) {
        event.setCancelled(true);
    }
}
```

### EconomyTransactionEvent

Fired for coin transactions.

```java
import com.skyblock.api.events.EconomyTransactionEvent;

@EventHandler
public void onTransaction(EconomyTransactionEvent event) {
    Player player = event.getPlayer();
    double amount = event.getAmount();
    EconomyTransactionEvent.Type type = event.getType(); // DEPOSIT, WITHDRAW, TRANSFER
    String reason = event.getReason();

    // This event is cancellable
    if (amount > 1000000) {
        event.setCancelled(true);
    }
}
```

## PlaceholderAPI Integration

If PlaceholderAPI is installed, these placeholders are available:

| Placeholder | Description |
|-------------|-------------|
| `%skyblock_coins%` | Player's coin balance (formatted) |
| `%skyblock_coins_raw%` | Player's coin balance (raw number) |
| `%skyblock_bank%` | Bank balance (Phase 2) |
| `%skyblock_skill_<skill>%` | Skill level |
| `%skyblock_skill_<skill>_xp%` | Skill XP |
| `%skyblock_skill_<skill>_progress%` | Progress to next level (%) |
| `%skyblock_collection_<id>%` | Collection amount |
| `%skyblock_collection_<id>_tier%` | Collection tier |
| `%skyblock_profile%` | Active profile name |
| `%skyblock_profile_count%` | Number of profiles |
| `%skyblock_stat_<stat>%` | Stat value |

Examples:
- `%skyblock_skill_mining%` → "35"
- `%skyblock_coins%` → "1.5M"
- `%skyblock_stat_STRENGTH%` → "150"

## Maven Dependency

To use the API in your plugin, add as a dependency:

```xml
<dependency>
    <groupId>com.skyblock</groupId>
    <artifactId>SkyblockFOSS</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

And in your `plugin.yml`:
```yaml
depend: [SkyblockFOSS]
# or
softdepend: [SkyblockFOSS]
```

## Example Plugin

```java
package com.example.skyblockadon;

import com.skyblock.SkyblockPlugin;
import com.skyblock.api.SkyblockAPI;
import com.skyblock.api.events.SkillLevelUpEvent;
import com.skyblock.skills.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyblockAddon extends JavaPlugin implements Listener {

    private SkyblockAPI api;

    @Override
    public void onEnable() {
        Plugin skyblock = getServer().getPluginManager().getPlugin("SkyblockFOSS");

        if (skyblock == null || !skyblock.isEnabled()) {
            getLogger().severe("SkyblockFOSS not found! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        api = ((SkyblockPlugin) skyblock).getAPI();
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("SkyblockAddon enabled!");
    }

    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();

        // Give bonus coins on level up
        if (event.getNewLevel() % 5 == 0) {
            double bonus = event.getNewLevel() * 1000;
            api.addCoins(player, bonus);
            player.sendMessage("Milestone bonus: +" + bonus + " coins!");
        }
    }

    // Custom command to show stats
    public void showStats(Player player) {
        int miningLevel = api.getSkillLevel(player, SkillType.MINING);
        double coins = api.getCoins(player);

        player.sendMessage("Mining: " + miningLevel);
        player.sendMessage("Coins: " + coins);
    }
}
```

## Error Handling

All API methods return sensible defaults if data is unavailable:
- Numeric values return `0` or `0.0`
- Objects return `null`
- Booleans return `false`
- Collections return empty collections

Always null-check object returns:

```java
SkyblockPlayer sbPlayer = api.getPlayer(player);
if (sbPlayer != null) {
    // Safe to use
}
```

## Thread Safety

- All API methods are safe to call from any thread
- Database operations are handled asynchronously internally
- Bukkit API calls are automatically scheduled to the main thread
