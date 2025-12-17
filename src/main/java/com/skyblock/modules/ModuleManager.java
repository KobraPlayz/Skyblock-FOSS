package com.skyblock.modules;

import com.skyblock.SkyblockPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages the modular system allowing features to be enabled/disabled.
 */
public class ModuleManager {

    private final SkyblockPlugin plugin;
    private final Map<String, Boolean> moduleStates;

    public ModuleManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.moduleStates = new HashMap<>();
        loadModuleStates();
    }

    /**
     * Load module states from configuration.
     */
    private void loadModuleStates() {
        FileConfiguration config = plugin.getConfigManager().getModulesConfig();
        if (config == null) {
            plugin.log(Level.WARNING, "Modules config not found, using defaults.");
            return;
        }

        // Load Phase 1 modules
        loadModuleSection(config, "modules");

        // Load Phase 2 modules (all disabled by default)
        loadModuleSection(config, "phase2");

        // Load Phase 3 modules (all disabled by default)
        loadModuleSection(config, "phase3");

        // Load Phase 4 modules (all disabled by default)
        loadModuleSection(config, "phase4");

        plugin.log(Level.INFO, "Loaded " + moduleStates.size() + " module states.");
    }

    /**
     * Load a section of modules from config.
     */
    private void loadModuleSection(FileConfiguration config, String sectionPath) {
        ConfigurationSection section = config.getConfigurationSection(sectionPath);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection moduleSection = section.getConfigurationSection(key);
            if (moduleSection != null) {
                boolean enabled = moduleSection.getBoolean("enabled", true);
                moduleStates.put(key, enabled);

                // Check for sub-modules
                ConfigurationSection individual = moduleSection.getConfigurationSection("individual");
                if (individual != null) {
                    for (String subKey : individual.getKeys(false)) {
                        moduleStates.put(key + "." + subKey, individual.getBoolean(subKey, true));
                    }
                }

                ConfigurationSection categories = moduleSection.getConfigurationSection("categories");
                if (categories != null) {
                    for (String catKey : categories.getKeys(false)) {
                        moduleStates.put(key + "." + catKey, categories.getBoolean(catKey, true));
                    }
                }
            } else {
                // Simple boolean value
                moduleStates.put(key, config.getBoolean(sectionPath + "." + key, true));
            }
        }
    }

    /**
     * Check if a module is enabled.
     */
    public boolean isModuleEnabled(String module) {
        return moduleStates.getOrDefault(module, true);
    }

    /**
     * Check if a sub-module is enabled.
     * For example: isSubModuleEnabled("skills", "mining")
     */
    public boolean isSubModuleEnabled(String parent, String child) {
        if (!isModuleEnabled(parent)) return false;
        return moduleStates.getOrDefault(parent + "." + child, true);
    }

    /**
     * Set a module's enabled state.
     */
    public void setModuleEnabled(String module, boolean enabled) {
        moduleStates.put(module, enabled);
    }

    /**
     * Check if a feature is coming soon (Phase 2-4).
     */
    public boolean isComingSoon(String module) {
        FileConfiguration config = plugin.getConfigManager().getModulesConfig();
        if (config == null) return false;

        // Check phase2
        if (config.contains("phase2." + module + ".show-coming-soon")) {
            return config.getBoolean("phase2." + module + ".show-coming-soon", true);
        }

        // Check phase3
        if (config.contains("phase3." + module + ".show-coming-soon")) {
            return config.getBoolean("phase3." + module + ".show-coming-soon", true);
        }

        // Check phase4
        if (config.contains("phase4." + module + ".show-coming-soon")) {
            return config.getBoolean("phase4." + module + ".show-coming-soon", true);
        }

        return false;
    }

    /**
     * Get all module states.
     */
    public Map<String, Boolean> getAllModuleStates() {
        return new HashMap<>(moduleStates);
    }

    /**
     * Reload module states from configuration.
     */
    public void reload() {
        moduleStates.clear();
        loadModuleStates();
    }
}
