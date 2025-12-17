package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.skills.SkillType;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Skills menu GUI.
 */
public class SkillsMenu extends AbstractGUI {

    public SkillsMenu(SkyblockPlugin plugin) {
        super(plugin, "&8Your Skills", 6);
    }

    @Override
    protected void build(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        fillBorder(createFiller());

        if (sbPlayer == null) {
            player.closeInventory();
            return;
        }

        PlayerProfile profile = sbPlayer.getActiveProfile();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22};
        int slotIndex = 0;

        for (SkillType skill : SkillType.values()) {
            if (slotIndex >= slots.length) break;
            if (!plugin.getSkillManager().isSkillEnabled(skill)) continue;

            int slot = slots[slotIndex++];
            int level = sbPlayer.getSkillLevel(skill.getConfigKey());
            double xp = sbPlayer.getSkillXp(skill.getConfigKey());
            int maxLevel = skill.getMaxLevel();

            double progress = plugin.getSkillManager().getProgressToNextLevel(xp, level);
            long currentLevelXp = plugin.getSkillManager().getXpForLevel(level);
            long nextLevelXp = plugin.getSkillManager().getXpForNextLevel(level);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Level: &e" + level + "&7/&a" + maxLevel);

            if (level < maxLevel) {
                lore.add("");
                lore.add("&7Progress to Level " + (level + 1) + ":");
                lore.add(createProgressBar(progress));
                lore.add("&7 " + NumberUtils.formatAbbreviated(xp - currentLevelXp) + "&7/&a" +
                        NumberUtils.formatAbbreviated(nextLevelXp - currentLevelXp) +
                        " &8(" + String.format("%.1f", progress) + "%)");
            } else {
                lore.add("");
                lore.add("&6&lMAXED OUT!");
            }

            lore.add("");
            lore.add("&7Total XP: &b" + NumberUtils.formatAbbreviated(xp));

            if (skill.isCosmetic()) {
                lore.add("");
                lore.add("&8This is a cosmetic skill");
            }

            lore.add("");
            lore.add("&eClick for more info!");

            setItem(slot, new ItemBuilder(skill.getIcon())
                    .name(skill.getDisplayName())
                    .lore(lore)
                    .hideFlags()
                    .build(), event -> {
                // Show detailed skill info
            });
        }

        // Skill average
        setItem(40, new ItemBuilder(Material.NETHER_STAR)
                .name("&a&lSkill Average")
                .lore(
                        "",
                        "&7Your average skill level",
                        "&7across all skills.",
                        "",
                        "&7Average: &6" + String.format("%.2f", sbPlayer.getSkillAverage())
                )
                .glow()
                .build());

        // Back button
        setItem(48, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new SkyblockMenu(plugin));
        });

        // Close button
        setItem(50, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private String createProgressBar(double percentage) {
        int length = 20;
        int filled = (int) Math.round(percentage / 100 * length);

        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("-");
            } else {
                if (i == filled) bar.append("&f");
                bar.append("-");
            }
        }

        return ColorUtils.colorize(bar.toString());
    }
}
