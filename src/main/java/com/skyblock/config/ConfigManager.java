package com.skyblock.config;

import com.skyblock.SkyblockPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages all configuration files for the plugin.
 */
public class ConfigManager {

    private final SkyblockPlugin plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;

    // Config file names
    public static final String CONFIG = "config";
    public static final String MODULES = "modules";
    public static final String SKILLS = "skills";
    public static final String COLLECTIONS = "collections";
    public static final String ITEMS = "items";
    public static final String MESSAGES = "messages";

    public ConfigManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
    }

    /**
     * Load all configuration files.
     */
    public void loadAllConfigs() {
        // Save defaults if they don't exist
        plugin.saveDefaultConfig();
        saveDefaultConfig(MODULES);
        saveDefaultConfig(SKILLS);
        saveDefaultConfig(COLLECTIONS);
        saveDefaultConfig(ITEMS);
        saveDefaultConfig(MESSAGES);

        // Load all configs
        loadConfig(CONFIG);
        loadConfig(MODULES);
        loadConfig(SKILLS);
        loadConfig(COLLECTIONS);
        loadConfig(ITEMS);
        loadConfig(MESSAGES);

        plugin.log(Level.INFO, "Loaded " + configs.size() + " configuration files.");
    }

    /**
     * Save a default config file from resources.
     */
    private void saveDefaultConfig(String name) {
        File file = new File(plugin.getDataFolder(), name + ".yml");
        if (!file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }
    }

    /**
     * Load a specific configuration file.
     */
    public void loadConfig(String name) {
        File file;
        if (name.equals(CONFIG)) {
            file = new File(plugin.getDataFolder(), "config.yml");
        } else {
            file = new File(plugin.getDataFolder(), name + ".yml");
        }

        if (!file.exists()) {
            saveDefaultConfig(name);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Look for defaults in jar
        InputStream defaultStream = plugin.getResource(name + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }

        configs.put(name, config);
        configFiles.put(name, file);
    }

    /**
     * Save a specific configuration file.
     */
    public void saveConfig(String name) {
        FileConfiguration config = configs.get(name);
        File file = configFiles.get(name);

        if (config == null || file == null) {
            plugin.log(Level.WARNING, "Attempted to save unknown config: " + name);
            return;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Could not save config " + name + ": " + e.getMessage());
        }
    }

    /**
     * Reload all configuration files.
     */
    public void reloadAllConfigs() {
        configs.clear();
        configFiles.clear();
        loadAllConfigs();
    }

    /**
     * Get the main config.
     */
    public FileConfiguration getConfig() {
        return configs.getOrDefault(CONFIG, plugin.getConfig());
    }

    /**
     * Get the modules config.
     */
    public FileConfiguration getModulesConfig() {
        return configs.get(MODULES);
    }

    /**
     * Get the skills config.
     */
    public FileConfiguration getSkillsConfig() {
        return configs.get(SKILLS);
    }

    /**
     * Get the collections config.
     */
    public FileConfiguration getCollectionsConfig() {
        return configs.get(COLLECTIONS);
    }

    /**
     * Get the items config.
     */
    public FileConfiguration getItemsConfig() {
        return configs.get(ITEMS);
    }

    /**
     * Get the messages config.
     */
    public FileConfiguration getMessagesConfig() {
        return configs.get(MESSAGES);
    }

    /**
     * Get a config by name.
     */
    public FileConfiguration getConfigByName(String name) {
        return configs.get(name);
    }

    /**
     * Get a message from the messages config with prefix.
     */
    public String getMessage(String path) {
        FileConfiguration messages = getMessagesConfig();
        if (messages == null) return path;

        String prefix = messages.getString("prefix", "&6&lSKYBLOCK &8» &7");
        String message = messages.getString(path, path);

        return prefix + message;
    }

    /**
     * Get a raw message without prefix.
     */
    public String getRawMessage(String path) {
        FileConfiguration messages = getMessagesConfig();
        if (messages == null) return path;
        return messages.getString(path, path);
    }

    /**
     * Get the plugin data folder.
     */
    public File getDataFolder() {
        return plugin.getDataFolder();
    }
}
