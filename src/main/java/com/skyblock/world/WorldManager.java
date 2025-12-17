package com.skyblock.world;

import com.skyblock.SkyblockPlugin;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages world creation, loading, and unloading.
 * Supports both standard Bukkit worlds and SlimeWorldManager (if available).
 */
public class WorldManager {

    private final SkyblockPlugin plugin;
    private final Map<String, WorldData> loadedWorlds;
    private final Map<String, Long> worldLastAccess;

    // Configuration
    private String hubWorldName;
    private String islandTemplateWorld;
    private String gardenTemplateWorld;
    private boolean useSlimeWorlds;
    private int autoUnloadMinutes;
    private int maxLoadedIslands;

    // SlimeWorldManager integration (optional)
    private boolean slimeWorldManagerAvailable;
    private Object slimePlugin; // SlimePlugin if available

    public WorldManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.loadedWorlds = new ConcurrentHashMap<>();
        this.worldLastAccess = new ConcurrentHashMap<>();

        loadConfig();
        checkSlimeWorldManager();

        // Start world unload task
        startUnloadTask();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfigManager().getWorldsConfig();

        hubWorldName = config.getString("worlds.hub.world_name", "world_hub");
        islandTemplateWorld = config.getString("worlds.islands.template_world", "island_template");
        gardenTemplateWorld = config.getString("worlds.garden.template_world", "garden_template");
        useSlimeWorlds = config.getBoolean("worlds.islands.use_slime_worlds", false);
        autoUnloadMinutes = config.getInt("worlds.islands.auto_unload_minutes", 10);
        maxLoadedIslands = config.getInt("worlds.islands.max_loaded", 50);
    }

    private void checkSlimeWorldManager() {
        try {
            if (Bukkit.getPluginManager().getPlugin("SlimeWorldManager") != null ||
                Bukkit.getPluginManager().getPlugin("AdvancedSlimeWorldManager") != null) {
                slimeWorldManagerAvailable = true;
                // Get SlimePlugin instance
                slimePlugin = Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
                if (slimePlugin == null) {
                    slimePlugin = Bukkit.getPluginManager().getPlugin("AdvancedSlimeWorldManager");
                }
                plugin.log(Level.INFO, "SlimeWorldManager detected - using optimized world loading");
            } else {
                slimeWorldManagerAvailable = false;
                plugin.log(Level.INFO, "SlimeWorldManager not found - using standard Bukkit worlds");
            }
        } catch (Exception e) {
            slimeWorldManagerAvailable = false;
            plugin.log(Level.WARNING, "Failed to check for SlimeWorldManager: " + e.getMessage());
        }
    }

    /**
     * Initialize the hub world.
     */
    public void initializeHubWorld() {
        World hub = Bukkit.getWorld(hubWorldName);
        if (hub == null) {
            plugin.log(Level.INFO, "Creating hub world: " + hubWorldName);
            WorldCreator creator = new WorldCreator(hubWorldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            hub = creator.createWorld();

            if (hub != null) {
                // Set hub world gamerules
                hub.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                hub.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                hub.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                hub.setTime(6000);
            }
        }

        if (hub != null) {
            loadedWorlds.put(hubWorldName, new WorldData(hubWorldName, WorldType.HUB, hub));
            plugin.log(Level.INFO, "Hub world loaded: " + hubWorldName);
        }
    }

    /**
     * Create a new island world for a player.
     */
    public CompletableFuture<World> createIslandWorld(String worldName, UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (useSlimeWorlds && slimeWorldManagerAvailable) {
                    return createSlimeWorld(worldName, ownerUuid);
                } else {
                    return createBukkitWorld(worldName, ownerUuid);
                }
            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Failed to create island world: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }).thenApplyAsync(world -> world, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    private World createBukkitWorld(String worldName, UUID ownerUuid) {
        // Check if template exists
        File templateDir = new File(Bukkit.getWorldContainer(), islandTemplateWorld);
        File targetDir = new File(Bukkit.getWorldContainer(), worldName);

        if (templateDir.exists()) {
            // Copy template
            copyDirectory(templateDir, targetDir);

            // Delete uid.dat to allow loading as new world
            File uidFile = new File(targetDir, "uid.dat");
            if (uidFile.exists()) {
                uidFile.delete();
            }
        }

        // Create or load the world
        final World[] result = new World[1];
        Bukkit.getScheduler().runTask(plugin, () -> {
            WorldCreator creator = new WorldCreator(worldName);

            if (!templateDir.exists()) {
                // Create void world if no template
                creator.type(WorldType.FLAT);
                creator.generatorSettings("{\"layers\": [], \"biome\": \"the_void\"}");
                creator.generateStructures(false);
            }

            result[0] = creator.createWorld();

            if (result[0] != null) {
                setupIslandWorld(result[0]);
                loadedWorlds.put(worldName, new WorldData(worldName, WorldType.ISLAND, result[0]));
                worldLastAccess.put(worldName, System.currentTimeMillis());
            }
        });

        // Wait for world creation
        int attempts = 0;
        while (result[0] == null && attempts < 100) {
            try {
                Thread.sleep(50);
                attempts++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return result[0];
    }

    private World createSlimeWorld(String worldName, UUID ownerUuid) {
        // SlimeWorldManager integration
        // This is a placeholder - actual implementation depends on ASWM API version
        plugin.log(Level.INFO, "Creating SlimeWorld: " + worldName);

        // Fall back to Bukkit world if SWM fails
        return createBukkitWorld(worldName, ownerUuid);
    }

    /**
     * Load an existing island world.
     */
    public CompletableFuture<World> loadIslandWorld(String worldName) {
        // Check if already loaded
        WorldData existing = loadedWorlds.get(worldName);
        if (existing != null && existing.getWorld() != null) {
            worldLastAccess.put(worldName, System.currentTimeMillis());
            return CompletableFuture.completedFuture(existing.getWorld());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if world folder exists
                File worldDir = new File(Bukkit.getWorldContainer(), worldName);
                if (!worldDir.exists()) {
                    return null;
                }

                final World[] result = new World[1];
                Bukkit.getScheduler().runTask(plugin, () -> {
                    WorldCreator creator = new WorldCreator(worldName);
                    result[0] = creator.createWorld();

                    if (result[0] != null) {
                        setupIslandWorld(result[0]);
                        loadedWorlds.put(worldName, new WorldData(worldName, WorldType.ISLAND, result[0]));
                        worldLastAccess.put(worldName, System.currentTimeMillis());
                    }
                });

                // Wait for world load
                int attempts = 0;
                while (result[0] == null && attempts < 100) {
                    Thread.sleep(50);
                    attempts++;
                }

                return result[0];
            } catch (Exception e) {
                plugin.log(Level.WARNING, "Failed to load island world: " + worldName);
                return null;
            }
        });
    }

    /**
     * Unload an island world.
     */
    public void unloadIslandWorld(String worldName) {
        WorldData worldData = loadedWorlds.get(worldName);
        if (worldData == null || worldData.getWorld() == null) {
            return;
        }

        World world = worldData.getWorld();

        // Don't unload if players are present
        if (!world.getPlayers().isEmpty()) {
            return;
        }

        // Teleport any remaining entities/save data
        world.save();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (Bukkit.unloadWorld(world, true)) {
                loadedWorlds.remove(worldName);
                worldLastAccess.remove(worldName);
                plugin.log(Level.INFO, "Unloaded island world: " + worldName);
            }
        });
    }

    /**
     * Setup game rules for an island world.
     */
    private void setupIslandWorld(World world) {
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setDifficulty(Difficulty.HARD);
    }

    /**
     * Teleport a player to the hub.
     */
    public void teleportToHub(Player player) {
        World hub = Bukkit.getWorld(hubWorldName);
        if (hub == null) {
            player.sendMessage("§cHub world not available!");
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getWorldsConfig();
        double x = config.getDouble("worlds.hub.spawn_x", 0);
        double y = config.getDouble("worlds.hub.spawn_y", 100);
        double z = config.getDouble("worlds.hub.spawn_z", 0);

        Location spawn = new Location(hub, x, y, z);
        player.teleport(spawn);
    }

    /**
     * Teleport a player to an island.
     */
    public void teleportToIsland(Player player, String worldName, Location spawnLocation) {
        loadIslandWorld(worldName).thenAccept(world -> {
            if (world == null) {
                player.sendMessage("§cFailed to load island!");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Location loc = spawnLocation != null ?
                    spawnLocation.clone() :
                    new Location(world, 0, 100, 0);
                loc.setWorld(world);
                player.teleport(loc);
            });
        });
    }

    /**
     * Start the automatic world unload task.
     */
    private void startUnloadTask() {
        long intervalTicks = autoUnloadMinutes * 60 * 20L; // Convert minutes to ticks

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long unloadThreshold = autoUnloadMinutes * 60 * 1000L;

            List<String> toUnload = new ArrayList<>();

            for (Map.Entry<String, Long> entry : worldLastAccess.entrySet()) {
                String worldName = entry.getKey();
                WorldData worldData = loadedWorlds.get(worldName);

                if (worldData != null && worldData.getType() == WorldType.ISLAND) {
                    World world = worldData.getWorld();

                    // Check if world should be unloaded
                    if (world != null && world.getPlayers().isEmpty() &&
                        now - entry.getValue() > unloadThreshold) {
                        toUnload.add(worldName);
                    }
                }
            }

            for (String worldName : toUnload) {
                unloadIslandWorld(worldName);
            }
        }, intervalTicks, intervalTicks);
    }

    /**
     * Copy a directory recursively.
     */
    private void copyDirectory(File source, File target) {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }

            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    // Skip session.lock and uid.dat
                    if (file.equals("session.lock") || file.equals("uid.dat")) {
                        continue;
                    }
                    copyDirectory(new File(source, file), new File(target, file));
                }
            }
        } else {
            try {
                java.nio.file.Files.copy(source.toPath(), target.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                plugin.log(Level.WARNING, "Failed to copy file: " + source.getName());
            }
        }
    }

    /**
     * Delete a world directory.
     */
    public boolean deleteWorld(String worldName) {
        // Unload first
        WorldData worldData = loadedWorlds.get(worldName);
        if (worldData != null && worldData.getWorld() != null) {
            // Teleport players out
            for (Player player : worldData.getWorld().getPlayers()) {
                teleportToHub(player);
            }
            Bukkit.unloadWorld(worldData.getWorld(), false);
        }

        loadedWorlds.remove(worldName);
        worldLastAccess.remove(worldName);

        // Delete world folder
        File worldDir = new File(Bukkit.getWorldContainer(), worldName);
        return deleteDirectory(worldDir);
    }

    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return dir.delete();
    }

    /**
     * Update last access time for a world.
     */
    public void updateWorldAccess(String worldName) {
        if (loadedWorlds.containsKey(worldName)) {
            worldLastAccess.put(worldName, System.currentTimeMillis());
        }
    }

    /**
     * Check if a world is loaded.
     */
    public boolean isWorldLoaded(String worldName) {
        return loadedWorlds.containsKey(worldName) &&
               loadedWorlds.get(worldName).getWorld() != null;
    }

    /**
     * Get the number of loaded island worlds.
     */
    public int getLoadedIslandCount() {
        return (int) loadedWorlds.values().stream()
            .filter(w -> w.getType() == WorldType.ISLAND)
            .count();
    }

    public String getHubWorldName() {
        return hubWorldName;
    }

    public boolean isSlimeWorldManagerAvailable() {
        return slimeWorldManagerAvailable;
    }

    public void shutdown() {
        // Save and unload all island worlds
        for (WorldData worldData : loadedWorlds.values()) {
            if (worldData.getType() == WorldType.ISLAND && worldData.getWorld() != null) {
                worldData.getWorld().save();
            }
        }
    }

    /**
     * World types.
     */
    public enum WorldType {
        HUB,
        ISLAND,
        GARDEN
    }

    /**
     * Data class for loaded worlds.
     */
    public static class WorldData {
        private final String name;
        private final WorldType type;
        private final World world;

        public WorldData(String name, WorldType type, World world) {
            this.name = name;
            this.type = type;
            this.world = world;
        }

        public String getName() {
            return name;
        }

        public WorldType getType() {
            return type;
        }

        public World getWorld() {
            return world;
        }
    }
}
