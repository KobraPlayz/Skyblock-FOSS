package com.skyblock.items.reforge;

import com.skyblock.SkyblockPlugin;
import com.skyblock.items.CustomItem;
import com.skyblock.items.ItemCategory;
import com.skyblock.items.rarity.Rarity;
import com.skyblock.items.stats.ItemStats;
import com.skyblock.items.stats.StatType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages reforges and their application to items.
 */
public class ReforgeManager {

    private final SkyblockPlugin plugin;
    private final Map<String, Reforge> weaponReforges;
    private final Map<String, Reforge> armorReforges;
    private final Map<String, Reforge> toolReforges;
    private final Map<String, Reforge> accessoryReforges;
    private final Random random;

    public ReforgeManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.weaponReforges = new HashMap<>();
        this.armorReforges = new HashMap<>();
        this.toolReforges = new HashMap<>();
        this.accessoryReforges = new HashMap<>();
        this.random = new Random();
    }

    /**
     * Load reforges from configuration.
     */
    public void loadReforges(FileConfiguration config) {
        weaponReforges.clear();
        armorReforges.clear();

        ConfigurationSection reforgesSection = config.getConfigurationSection("reforges");
        if (reforgesSection == null) {
            plugin.log(Level.WARNING, "No reforges section found!");
            return;
        }

        // Load weapon reforges
        loadReforgeType(reforgesSection, "weapon", weaponReforges);

        // Load armor reforges
        loadReforgeType(reforgesSection, "armor", armorReforges);

        int total = weaponReforges.size() + armorReforges.size();
        plugin.log(Level.INFO, "Loaded " + total + " reforges.");
    }

    /**
     * Load reforges of a specific type.
     */
    private void loadReforgeType(ConfigurationSection section, String type, Map<String, Reforge> targetMap) {
        ConfigurationSection typeSection = section.getConfigurationSection(type);
        if (typeSection == null) return;

        for (String reforgeId : typeSection.getKeys(false)) {
            ConfigurationSection reforgeSection = typeSection.getConfigurationSection(reforgeId);
            if (reforgeSection == null) continue;

            Reforge reforge = new Reforge(reforgeId, reforgeId, type);

            // Load stats for each rarity
            ConfigurationSection statsSection = reforgeSection.getConfigurationSection("stats");
            if (statsSection != null) {
                for (String rarityName : statsSection.getKeys(false)) {
                    Rarity rarity = Rarity.fromString(rarityName);
                    ConfigurationSection rarityStats = statsSection.getConfigurationSection(rarityName);

                    if (rarityStats != null) {
                        ItemStats stats = new ItemStats();
                        for (String statKey : rarityStats.getKeys(false)) {
                            StatType statType = StatType.fromString(statKey);
                            if (statType != null) {
                                stats.setStat(statType, rarityStats.getDouble(statKey));
                            }
                        }
                        reforge.setStats(rarity, stats);
                    }
                }
            }

            targetMap.put(reforgeId.toUpperCase(), reforge);
        }
    }

    /**
     * Get a random reforge for an item category.
     */
    public Reforge getRandomReforge(ItemCategory category) {
        Map<String, Reforge> reforges = getReforgesForCategory(category);
        if (reforges.isEmpty()) return null;

        List<Reforge> list = new ArrayList<>(reforges.values());
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Get a specific reforge by ID.
     */
    public Reforge getReforge(String id, ItemCategory category) {
        Map<String, Reforge> reforges = getReforgesForCategory(category);
        return reforges.get(id.toUpperCase());
    }

    /**
     * Get all reforges for a category.
     */
    public Map<String, Reforge> getReforgesForCategory(ItemCategory category) {
        if (category.isWeapon()) return weaponReforges;
        if (category.isArmor()) return armorReforges;
        if (category.isTool()) return toolReforges;
        if (category == ItemCategory.ACCESSORY) return accessoryReforges;
        return new HashMap<>();
    }

    /**
     * Get all weapon reforges.
     */
    public Collection<Reforge> getWeaponReforges() {
        return weaponReforges.values();
    }

    /**
     * Get all armor reforges.
     */
    public Collection<Reforge> getArmorReforges() {
        return armorReforges.values();
    }

    /**
     * Calculate the cost to reforge an item.
     */
    public int getReforgeCost(Rarity rarity) {
        return rarity.getReforgeCost();
    }

    /**
     * Get all reforge names for tab completion.
     */
    public List<String> getAllReforgeNames() {
        List<String> names = new ArrayList<>();
        names.addAll(weaponReforges.keySet());
        names.addAll(armorReforges.keySet());
        names.addAll(toolReforges.keySet());
        names.addAll(accessoryReforges.keySet());
        return names;
    }
}
