package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.garden.Garden;
import com.skyblock.garden.GardenPlot;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Garden Plots overview menu.
 */
public class GardenPlotsMenu extends AbstractGUI {

    private final UUID playerUuid;

    public GardenPlotsMenu(SkyblockPlugin plugin, UUID playerUuid) {
        super(plugin, "&e&lGarden Plots", 6);
        this.playerUuid = playerUuid;
    }

    @Override
    protected void build(Player player) {
        Garden garden = plugin.getGardenManager().getGarden(playerUuid);
        if (garden == null) {
            player.closeInventory();
            return;
        }

        fillBorder(createFiller());

        // Info
        setItem(4, new ItemBuilder(Material.MAP)
                .name("&e&lGarden Plots")
                .lore(
                        "&7Your garden has 24 plots!",
                        "",
                        "&7Plot Status Legend:",
                        "&8■ &7Locked - Purchase to unlock",
                        "&e■ &7Unlocked - Clean to use",
                        "&a■ &7Cleaned - Ready for farming",
                        "&b■ &7Active - Has preset applied"
                )
                .build());

        // Display plots in a 6x4 grid (24 plots)
        List<GardenPlot> plots = garden.getPlots();

        // Slots for 24 plots (avoiding borders)
        int[] plotSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39
        };

        for (int i = 0; i < Math.min(plots.size(), plotSlots.length); i++) {
            GardenPlot plot = plots.get(i);
            int plotIndex = i;

            setItem(plotSlots[i], createPlotItem(plot, i + 1, garden), event -> {
                handlePlotClick(player, garden, plot, plotIndex);
            });
        }

        // Back button
        setItem(45, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenMenu(plugin, playerUuid));
        });

        // Close button
        setItem(53, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private ItemStack createPlotItem(GardenPlot plot, int plotNumber, Garden garden) {
        Material material;
        String statusName;
        String[] statusLore;

        switch (plot.getStatus()) {
            case LOCKED:
                material = Material.GRAY_STAINED_GLASS_PANE;
                statusName = "&8Plot #" + plotNumber + " (Locked)";
                statusLore = new String[] {
                        "&7This plot is locked!",
                        "",
                        "&7Unlock with:",
                        "&6- 1,000 Copper from SkyMart",
                        "&7- or Garden Level " + (plotNumber * 2),
                        "",
                        "&cClick to unlock (if possible)"
                };
                break;
            case UNLOCKED:
                material = Material.YELLOW_STAINED_GLASS_PANE;
                statusName = "&e&lPlot #" + plotNumber + " (Unlocked)";
                statusLore = new String[] {
                        "&7This plot needs cleaning!",
                        "",
                        "&7Cleaning Cost:",
                        "&21,000 Compost",
                        "",
                        "&7Your Compost: &2" + garden.getCompost(),
                        "",
                        garden.getCompost() >= 1000 ? "&eClick to clean!" : "&cNeed more compost!"
                };
                break;
            case CLEANED:
                material = Material.LIME_STAINED_GLASS_PANE;
                statusName = "&a&lPlot #" + plotNumber + " (Cleaned)";
                statusLore = new String[] {
                        "&7This plot is ready for farming!",
                        "",
                        "&7Size: &e96x96 blocks",
                        "",
                        "&7Apply a preset or plant",
                        "&7crops manually.",
                        "",
                        "&eClick to teleport!"
                };
                break;
            case PRESET_APPLIED:
                material = Material.CYAN_STAINED_GLASS_PANE;
                statusName = "&b&lPlot #" + plotNumber + " (Active)";
                String preset = plot.getPreset() != null ? plot.getPreset() : "Custom";
                statusLore = new String[] {
                        "&7Preset: &b" + preset,
                        "",
                        "&7This plot is actively",
                        "&7producing crops!",
                        "",
                        "&eClick to teleport!"
                };
                break;
            default:
                material = Material.BARRIER;
                statusName = "&cPlot #" + plotNumber;
                statusLore = new String[] {"&7Unknown status"};
        }

        return new ItemBuilder(material)
                .name(statusName)
                .lore(statusLore)
                .build();
    }

    private void handlePlotClick(Player player, Garden garden, GardenPlot plot, int plotIndex) {
        switch (plot.getStatus()) {
            case LOCKED:
                // Check if player can unlock
                int requiredLevel = (plotIndex + 1) * 2;
                if (garden.getLevel() >= requiredLevel) {
                    plot.setStatus(GardenPlot.Status.UNLOCKED);
                    plugin.getGardenManager().saveGarden(garden);
                    player.sendMessage("§aPlot #" + (plotIndex + 1) + " unlocked!");
                    playSuccessSound(player);
                    plugin.getGuiManager().openGUI(player, new GardenPlotsMenu(plugin, playerUuid));
                } else {
                    player.sendMessage("§cYou need Garden Level " + requiredLevel + " or purchase from SkyMart!");
                    playErrorSound(player);
                }
                break;

            case UNLOCKED:
                // Try to clean the plot
                if (garden.getCompost() >= 1000) {
                    garden.setCompost(garden.getCompost() - 1000);
                    plot.setStatus(GardenPlot.Status.CLEANED);
                    plugin.getGardenManager().saveGarden(garden);
                    player.sendMessage("§aPlot #" + (plotIndex + 1) + " cleaned and ready for farming!");
                    playSuccessSound(player);
                    plugin.getGuiManager().openGUI(player, new GardenPlotsMenu(plugin, playerUuid));
                } else {
                    player.sendMessage("§cYou need 1,000 compost to clean this plot!");
                    playErrorSound(player);
                }
                break;

            case CLEANED:
            case PRESET_APPLIED:
                // Teleport to plot
                player.closeInventory();
                player.sendMessage("§aTeleporting to Plot #" + (plotIndex + 1) + "...");
                // Calculate plot location based on index
                // This would teleport to the specific plot location
                plugin.getGardenManager().teleportToGarden(player);
                break;
        }
    }
}
