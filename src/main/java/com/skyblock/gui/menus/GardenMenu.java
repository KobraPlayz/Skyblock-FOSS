package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.garden.CropType;
import com.skyblock.garden.Garden;
import com.skyblock.garden.GardenPlot;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main Garden GUI menu.
 */
public class GardenMenu extends AbstractGUI {

    private final UUID playerUuid;

    public GardenMenu(SkyblockPlugin plugin, UUID playerUuid) {
        super(plugin, "&2Garden Menu", 6);
        this.playerUuid = playerUuid;
    }

    @Override
    protected void build(Player player) {
        Garden garden = plugin.getGardenManager().getGarden(playerUuid);
        if (garden == null) {
            player.closeInventory();
            player.sendMessage("§cYou haven't unlocked the Garden yet! Reach SkyBlock Level 5.");
            return;
        }

        fillBorder(createFiller());

        // Garden Overview
        setItem(4, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&2&lYour Garden")
                .lore(
                        "&7Level: &a" + garden.getLevel(),
                        "&7XP: &e" + NumberUtils.format(garden.getXp()) + "/" + NumberUtils.format(getXpForLevel(garden.getLevel() + 1)),
                        "",
                        "&6Copper: &e" + NumberUtils.format(garden.getCopper()),
                        "&2Compost: &e" + NumberUtils.format(garden.getCompost()),
                        "",
                        "&7Unlocked Plots: &a" + countUnlockedPlots(garden) + "/24"
                )
                .build());

        // Desk - Crop Upgrades
        setItem(20, new ItemBuilder(Material.CARTOGRAPHY_TABLE)
                .name("&e&lDesk")
                .lore(
                        "&7Upgrade your crops to increase",
                        "&7Farming Fortune and yields!",
                        "",
                        "&7Each crop upgrade grants:",
                        "&a+5 Farming Fortune &7per level",
                        "",
                        "&eClick to view upgrades!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenUpgradesMenu(plugin, playerUuid));
        });

        // SkyMart - Garden Shop
        setItem(22, new ItemBuilder(Material.EMERALD)
                .name("&a&lSkyMart")
                .lore(
                        "&7The Garden's marketplace!",
                        "",
                        "&7Purchase seeds, farming tools,",
                        "&7and special items.",
                        "",
                        "&6Your Copper: &e" + NumberUtils.format(garden.getCopper()),
                        "",
                        "&eClick to shop!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenShopMenu(plugin, playerUuid));
        });

        // Milestones
        setItem(24, new ItemBuilder(Material.GOLDEN_CARROT)
                .name("&6&lCrop Milestones")
                .lore(
                        "&7Track your farming progress",
                        "&7for each crop type!",
                        "",
                        "&7Earn rewards and bonuses",
                        "&7as you harvest more crops.",
                        "",
                        "&eClick to view milestones!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenMilestonesMenu(plugin, playerUuid));
        });

        // Visitors
        int activeVisitors = garden.getActiveVisitors().size();
        setItem(30, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
                .name("&b&lVisitors")
                .lore(
                        "&7Garden visitors request items",
                        "&7in exchange for Copper!",
                        "",
                        "&7Active Visitors: &e" + activeVisitors + "/5",
                        "",
                        "&eClick to view visitors!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenVisitorsMenu(plugin, playerUuid));
        });

        // Composter
        setItem(32, new ItemBuilder(Material.COMPOSTER)
                .name("&2&lComposter")
                .lore(
                        "&7Convert crops and organic",
                        "&7materials into compost!",
                        "",
                        "&7Your Compost: &e" + NumberUtils.format(garden.getCompost()),
                        "",
                        "&7Compost is used to speed up",
                        "&7crop growth and unlock plots.",
                        "",
                        "&eClick to compost!"
                )
                .build(), event -> {
            // Composting functionality
            player.sendMessage("§2Composter coming soon!");
        });

        // Plots Overview
        setItem(40, new ItemBuilder(Material.MAP)
                .name("&e&lGarden Plots")
                .lore(
                        "&7View and manage your",
                        "&7garden plots!",
                        "",
                        "&7Unlocked: &a" + countUnlockedPlots(garden) + "/24",
                        "&7Cleaned: &e" + countCleanedPlots(garden) + "/" + countUnlockedPlots(garden),
                        "",
                        "&eClick to view plots!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenPlotsMenu(plugin, playerUuid));
        });

        // Teleport to Garden
        setItem(38, new ItemBuilder(Material.ENDER_PEARL)
                .name("&d&lTeleport to Garden")
                .lore(
                        "&7Teleport to your Garden!",
                        "",
                        "&eClick to teleport!"
                )
                .build(), event -> {
            player.closeInventory();
            plugin.getGardenManager().teleportToGarden(player);
        });

        // Farming Stats
        setItem(42, new ItemBuilder(Material.BOOK)
                .name("&6&lFarming Stats")
                .lore(
                        "&7Your farming statistics:",
                        "",
                        buildCropStats(garden)
                )
                .build());

        // Back button
        setItem(45, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new SkyblockMenu(plugin));
        });

        // Close button
        setItem(53, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private int countUnlockedPlots(Garden garden) {
        int count = 0;
        for (GardenPlot plot : garden.getPlots()) {
            if (plot.getStatus() != GardenPlot.Status.LOCKED) {
                count++;
            }
        }
        return count;
    }

    private int countCleanedPlots(Garden garden) {
        int count = 0;
        for (GardenPlot plot : garden.getPlots()) {
            if (plot.getStatus() == GardenPlot.Status.CLEANED || plot.getStatus() == GardenPlot.Status.PRESET_APPLIED) {
                count++;
            }
        }
        return count;
    }

    private long getXpForLevel(int level) {
        // Simple exponential XP curve
        return (long) (1000 * Math.pow(1.5, level - 1));
    }

    private String[] buildCropStats(Garden garden) {
        List<String> stats = new ArrayList<>();
        for (CropType crop : CropType.values()) {
            Garden.CropMilestone milestone = garden.getMilestone(crop);
            if (milestone != null && milestone.getTotalHarvested() > 0) {
                stats.add("&7" + formatCropName(crop) + ": &e" + NumberUtils.format(milestone.getTotalHarvested()));
            }
        }
        if (stats.isEmpty()) {
            stats.add("&7No crops harvested yet!");
        }
        return stats.toArray(new String[0]);
    }

    private String formatCropName(CropType crop) {
        String name = crop.name().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
