package com.skyblock;

import com.skyblock.api.SkyblockAPI;
import com.skyblock.commands.*;
import com.skyblock.config.ConfigManager;
import com.skyblock.database.DatabaseManager;
import com.skyblock.economy.EconomyManager;
import com.skyblock.collections.CollectionManager;
import com.skyblock.gui.GUIManager;
import com.skyblock.items.ItemManager;
import com.skyblock.modules.ModuleManager;
import com.skyblock.player.PlayerManager;
import com.skyblock.skills.SkillManager;
import com.skyblock.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for SkyblockFOSS
 * A comprehensive Hypixel Skyblock recreation for Minecraft 1.20.4
 *
 * Phase 1 - Core Foundation:
 * - Custom item system with stats, rarities, reforges
 * - Skills system (all skills with progression)
 * - Collections system
 * - SkyBlock main menu GUI
 * - Basic economy (coins, NPC shops)
 * - Profile system
 * - Admin GUI framework
 * - Database architecture for all phases
 */
public class SkyblockPlugin extends JavaPlugin {

    private static SkyblockPlugin instance;

    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ModuleManager moduleManager;
    private PlayerManager playerManager;
    private ItemManager itemManager;
    private SkillManager skillManager;
    private CollectionManager collectionManager;
    private EconomyManager economyManager;
    private GUIManager guiManager;

    // API
    private SkyblockAPI api;

    @Override
    public void onEnable() {
        instance = this;

        // Display startup banner
        logBanner();

        try {
            // Initialize configuration
            log(Level.INFO, "Loading configuration...");
            configManager = new ConfigManager(this);
            configManager.loadAllConfigs();

            // Initialize database
            log(Level.INFO, "Initializing database...");
            databaseManager = new DatabaseManager(this);
            if (!databaseManager.initialize()) {
                log(Level.SEVERE, "Failed to initialize database! Disabling plugin...");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // Initialize module manager
            log(Level.INFO, "Loading modules...");
            moduleManager = new ModuleManager(this);

            // Initialize managers
            log(Level.INFO, "Initializing managers...");
            playerManager = new PlayerManager(this);
            itemManager = new ItemManager(this);
            skillManager = new SkillManager(this);
            collectionManager = new CollectionManager(this);
            economyManager = new EconomyManager(this);
            guiManager = new GUIManager(this);

            // Initialize API
            api = new SkyblockAPI(this);

            // Register commands
            log(Level.INFO, "Registering commands...");
            registerCommands();

            // Register listeners
            log(Level.INFO, "Registering listeners...");
            registerListeners();

            // Load online players (for reloads)
            Bukkit.getOnlinePlayers().forEach(player -> playerManager.loadPlayer(player));

            // Setup external hooks
            setupHooks();

            // Start auto-save task
            startAutoSave();

            log(Level.INFO, "");
            log(Level.INFO, ColorUtils.colorize("&a&lSkyblockFOSS has been enabled successfully!"));
            log(Level.INFO, ColorUtils.colorize("&7Phase 1 - Core Foundation"));
            log(Level.INFO, "");

        } catch (Exception e) {
            log(Level.SEVERE, "Failed to enable SkyblockFOSS: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        log(Level.INFO, "Disabling SkyblockFOSS...");

        // Save all player data
        if (playerManager != null) {
            Bukkit.getOnlinePlayers().forEach(player -> playerManager.savePlayer(player.getUniqueId()));
        }

        // Close database connections
        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        // Close all GUIs
        if (guiManager != null) {
            guiManager.closeAll();
        }

        log(Level.INFO, "SkyblockFOSS has been disabled.");
        instance = null;
    }

    private void logBanner() {
        getLogger().info("");
        getLogger().info("  _____ _          _     _            _    ");
        getLogger().info(" / ____| |        | |   | |          | |   ");
        getLogger().info("| (___ | | ___   _| |__ | | ___   ___| | __");
        getLogger().info(" \\___ \\| |/ / | | | '_ \\| |/ _ \\ / __| |/ /");
        getLogger().info(" ____) |   <| |_| | |_) | | (_) | (__|   < ");
        getLogger().info("|_____/|_|\\_\\\\__, |_.__/|_|\\___/ \\___|_|\\_\\");
        getLogger().info("              __/ |  FOSS v" + getDescription().getVersion());
        getLogger().info("             |___/   Phase 1 - Core Foundation");
        getLogger().info("");
    }

    private void registerCommands() {
        getCommand("skyblock").setExecutor(new SkyblockCommand(this));
        getCommand("skills").setExecutor(new SkillsCommand(this));
        getCommand("collections").setExecutor(new CollectionsCommand(this));
        getCommand("profile").setExecutor(new ProfileCommand(this));
        getCommand("sbadmin").setExecutor(new AdminCommand(this));
        getCommand("coins").setExecutor(new CoinsCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
    }

    private void registerListeners() {
        // Player join/quit listeners
        getServer().getPluginManager().registerEvents(playerManager, this);

        // GUI listener
        getServer().getPluginManager().registerEvents(guiManager, this);

        // Item listener
        getServer().getPluginManager().registerEvents(itemManager, this);

        // Skill listeners (if enabled)
        if (moduleManager.isModuleEnabled("skills")) {
            skillManager.registerListeners();
        }

        // Collection listeners (if enabled)
        if (moduleManager.isModuleEnabled("collections")) {
            collectionManager.registerListeners();
        }
    }

    private void setupHooks() {
        // Vault hook
        if (configManager.getConfig().getBoolean("hooks.vault", true)) {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                log(Level.INFO, "Hooked into Vault!");
            }
        }

        // PlaceholderAPI hook
        if (configManager.getConfig().getBoolean("hooks.placeholderapi", true)) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                // Register placeholders
                log(Level.INFO, "Hooked into PlaceholderAPI!");
            }
        }
    }

    private void startAutoSave() {
        int interval = configManager.getConfig().getInt("general.auto-save-interval", 300) * 20; // Convert to ticks
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (configManager.getConfig().getBoolean("general.debug", false)) {
                log(Level.INFO, "Running auto-save...");
            }
            Bukkit.getOnlinePlayers().forEach(player -> playerManager.savePlayer(player.getUniqueId()));
        }, interval, interval);
    }

    public void reload() {
        log(Level.INFO, "Reloading SkyblockFOSS...");
        configManager.loadAllConfigs();
        itemManager.reload();
        skillManager.reload();
        collectionManager.reload();
        log(Level.INFO, "Reload complete!");
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public void debug(String message) {
        if (configManager != null && configManager.getConfig().getBoolean("general.debug", false)) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }

    // Getters
    public static SkyblockPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public SkyblockAPI getAPI() {
        return api;
    }
}
