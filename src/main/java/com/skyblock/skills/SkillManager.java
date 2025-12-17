package com.skyblock.skills;

import com.skyblock.SkyblockPlugin;
import com.skyblock.api.events.SkillLevelUpEvent;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.skills.listeners.*;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages the skills system including XP, levels, and rewards.
 */
public class SkillManager {

    private final SkyblockPlugin plugin;
    private final Map<Integer, Long> xpRequirements;
    private final Map<SkillType, Map<String, Double>> xpSources;

    public SkillManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.xpRequirements = new LinkedHashMap<>();
        this.xpSources = new EnumMap<>(SkillType.class);

        loadSkillData();
    }

    /**
     * Load skill data from configuration.
     */
    private void loadSkillData() {
        FileConfiguration config = plugin.getConfigManager().getSkillsConfig();
        if (config == null) {
            plugin.log(Level.WARNING, "Skills config not found!");
            return;
        }

        // Load XP requirements
        ConfigurationSection xpSection = config.getConfigurationSection("xp-requirements");
        if (xpSection != null) {
            for (String level : xpSection.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(level);
                    long xp = xpSection.getLong(level);
                    xpRequirements.put(lvl, xp);
                } catch (NumberFormatException ignored) {}
            }
        }

        // Load XP sources for each skill
        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
        if (skillsSection != null) {
            for (String skillKey : skillsSection.getKeys(false)) {
                SkillType skillType = SkillType.fromString(skillKey);
                if (skillType == null) continue;

                ConfigurationSection skillSection = skillsSection.getConfigurationSection(skillKey);
                if (skillSection == null) continue;

                ConfigurationSection sourcesSection = skillSection.getConfigurationSection("xp-sources");
                if (sourcesSection != null) {
                    Map<String, Double> sources = new HashMap<>();
                    for (String source : sourcesSection.getKeys(false)) {
                        sources.put(source.toUpperCase(), sourcesSection.getDouble(source));
                    }
                    xpSources.put(skillType, sources);
                }
            }
        }

        plugin.log(Level.INFO, "Loaded " + xpRequirements.size() + " XP levels and " + xpSources.size() + " skill configurations.");
    }

    /**
     * Register skill listeners.
     */
    public void registerListeners() {
        if (plugin.getModuleManager().isSubModuleEnabled("skills", "mining")) {
            plugin.getServer().getPluginManager().registerEvents(new MiningListener(plugin), plugin);
        }
        if (plugin.getModuleManager().isSubModuleEnabled("skills", "farming")) {
            plugin.getServer().getPluginManager().registerEvents(new FarmingListener(plugin), plugin);
        }
        if (plugin.getModuleManager().isSubModuleEnabled("skills", "combat")) {
            plugin.getServer().getPluginManager().registerEvents(new CombatListener(plugin), plugin);
        }
        if (plugin.getModuleManager().isSubModuleEnabled("skills", "foraging")) {
            plugin.getServer().getPluginManager().registerEvents(new ForagingListener(plugin), plugin);
        }
        if (plugin.getModuleManager().isSubModuleEnabled("skills", "fishing")) {
            plugin.getServer().getPluginManager().registerEvents(new FishingListener(plugin), plugin);
        }
    }

    /**
     * Add XP to a skill for a player.
     */
    public void addXp(SkyblockPlayer player, String skill, double xp) {
        if (xp <= 0) return;

        PlayerProfile profile = player.getActiveProfile();
        if (profile == null) return;

        SkillType skillType = SkillType.fromString(skill);
        if (skillType == null) return;

        // Check if skill is enabled
        if (!plugin.getModuleManager().isSubModuleEnabled("skills", skill)) return;

        PlayerProfile.SkillData skillData = profile.getSkillData(skill);
        if (skillData == null) {
            profile.setSkillData(skill, 0, 0);
            skillData = profile.getSkillData(skill);
        }

        int currentLevel = skillData.getLevel();
        int maxLevel = skillType.getMaxLevel();

        // Check if already max level
        if (currentLevel >= maxLevel) return;

        double currentXp = skillData.getXp();
        double newXp = currentXp + xp;
        skillData.setXp(newXp);

        // Check for level up
        int newLevel = calculateLevel(newXp);
        if (newLevel > currentLevel && newLevel <= maxLevel) {
            skillData.setLevel(Math.min(newLevel, maxLevel));
            onLevelUp(player, skillType, currentLevel, Math.min(newLevel, maxLevel));
        }

        // Show action bar
        showXpGain(player, skillType, xp, newXp, newLevel);

        // Invalidate stats cache
        player.invalidateStatsCache();
    }

    /**
     * Calculate the level from total XP.
     */
    public int calculateLevel(double totalXp) {
        int level = 0;
        for (Map.Entry<Integer, Long> entry : xpRequirements.entrySet()) {
            if (totalXp >= entry.getValue()) {
                level = entry.getKey();
            } else {
                break;
            }
        }
        return level;
    }

    /**
     * Get XP required for a level.
     */
    public long getXpForLevel(int level) {
        return xpRequirements.getOrDefault(level, 0L);
    }

    /**
     * Get XP required for the next level.
     */
    public long getXpForNextLevel(int currentLevel) {
        return xpRequirements.getOrDefault(currentLevel + 1, 0L);
    }

    /**
     * Get progress to next level as percentage.
     */
    public double getProgressToNextLevel(double currentXp, int currentLevel) {
        long currentLevelXp = getXpForLevel(currentLevel);
        long nextLevelXp = getXpForNextLevel(currentLevel);

        if (nextLevelXp <= currentLevelXp) return 100.0;

        double progress = (currentXp - currentLevelXp) / (double) (nextLevelXp - currentLevelXp) * 100;
        return Math.min(100.0, Math.max(0.0, progress));
    }

    /**
     * Get XP source value for a skill and source.
     */
    public double getXpSource(SkillType skill, String source) {
        Map<String, Double> sources = xpSources.get(skill);
        if (sources == null) return 0;
        return sources.getOrDefault(source.toUpperCase(), 0.0);
    }

    /**
     * Handle level up.
     */
    private void onLevelUp(SkyblockPlayer player, SkillType skill, int oldLevel, int newLevel) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) return;

        // Fire event
        SkillLevelUpEvent event = new SkillLevelUpEvent(bukkitPlayer, skill, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        // Send level up message
        String message = plugin.getConfigManager().getRawMessage("skills.level-up")
                .replace("{skill}", skill.getDisplayName())
                .replace("{prev_level}", String.valueOf(oldLevel))
                .replace("{new_level}", String.valueOf(newLevel));
        bukkitPlayer.sendMessage(ColorUtils.colorize(message));

        // Play sound
        if (plugin.getModuleManager().isModuleEnabled("sounds")) {
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        // Show title for milestone levels
        if (newLevel % 5 == 0 || newLevel == skill.getMaxLevel()) {
            String title = newLevel == skill.getMaxLevel() ?
                    ColorUtils.colorize("&6&lMAX LEVEL!") :
                    ColorUtils.colorize("&b&lSKILL LEVEL UP!");
            String subtitle = ColorUtils.colorize(skill.getDisplayName() + " &7" + oldLevel + " ➜ &a" + newLevel);
            bukkitPlayer.sendTitle(title, subtitle, 10, 40, 20);
        }
    }

    /**
     * Show XP gain in action bar.
     */
    private void showXpGain(SkyblockPlayer player, SkillType skill, double xpGained, double totalXp, int level) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) return;

        FileConfiguration config = plugin.getConfigManager().getSkillsConfig();
        if (!config.getBoolean("settings.action-bar.enabled", true)) return;

        double progress = getProgressToNextLevel(totalXp, level);
        String format = config.getString("settings.action-bar.format",
                "&b{skill} &7{current_xp}/{next_level_xp} &8(&a{percent}%&8)");

        long currentLevelXp = getXpForLevel(level);
        long nextLevelXp = getXpForNextLevel(level);

        String message = format
                .replace("{skill}", skill.getId())
                .replace("{current_xp}", NumberUtils.formatAbbreviated(totalXp - currentLevelXp))
                .replace("{next_level_xp}", NumberUtils.formatAbbreviated(nextLevelXp - currentLevelXp))
                .replace("{percent}", NumberUtils.formatPercent(progress).replace("%", ""));

        bukkitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ColorUtils.colorize(message)));
    }

    /**
     * Reload skill data.
     */
    public void reload() {
        xpRequirements.clear();
        xpSources.clear();
        loadSkillData();
    }

    /**
     * Get all skill types.
     */
    public SkillType[] getAllSkillTypes() {
        return SkillType.values();
    }

    /**
     * Check if a skill is enabled.
     */
    public boolean isSkillEnabled(SkillType skill) {
        return plugin.getModuleManager().isSubModuleEnabled("skills", skill.getConfigKey());
    }
}
