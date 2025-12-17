package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Admin panel menu.
 */
public class AdminMenu extends AbstractGUI {

    public AdminMenu(SkyblockPlugin plugin) {
        super(plugin, "&8Admin Panel", 4);
    }

    @Override
    protected void build(Player player) {
        if (!player.hasPermission("skyblock.admin")) {
            player.closeInventory();
            return;
        }

        fillBorder(createFiller());

        // Player Management
        setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .name("&a&lPlayer Management")
                .lore(
                        "&7Manage player data, skills,",
                        "&7collections, and economy.",
                        "",
                        "&eClick to open!"
                )
                .build(), event -> {
            // Open player selection menu
            player.sendMessage(ColorUtils.colorize("&7Use &e/sbadmin player <name> &7to manage a player."));
        });

        // Item Spawner
        setItem(12, new ItemBuilder(Material.CHEST)
                .name("&e&lItem Spawner")
                .lore(
                        "&7Spawn custom Skyblock items.",
                        "",
                        "&eClick to open!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new ItemSpawnerMenu(plugin));
        });

        // Economy Management
        setItem(14, new ItemBuilder(Material.GOLD_BLOCK)
                .name("&6&lEconomy Management")
                .lore(
                        "&7Manage server economy.",
                        "",
                        "&7Use commands:",
                        "&e/sbadmin coins <player> give <amount>",
                        "&e/sbadmin coins <player> take <amount>",
                        "&e/sbadmin coins <player> set <amount>"
                )
                .build());

        // Reload Config
        setItem(16, new ItemBuilder(Material.REDSTONE)
                .name("&c&lReload Configuration")
                .lore(
                        "&7Reload all config files.",
                        "",
                        "&cWarning: May cause brief lag!",
                        "",
                        "&eClick to reload!"
                )
                .build(), event -> {
            plugin.reload();
            playSuccessSound(player);
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.reload-success")));
        });

        // Server Info
        int playerCount = Bukkit.getOnlinePlayers().size();
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;

        setItem(22, new ItemBuilder(Material.PAPER)
                .name("&b&lServer Info")
                .lore(
                        "",
                        "&7Online Players: &e" + playerCount,
                        "&7Memory: &e" + usedMemory + "MB&7/&a" + maxMemory + "MB",
                        "&7TPS: &e" + String.format("%.1f", Bukkit.getTPS()[0]),
                        "",
                        "&7Plugin Version: &a" + plugin.getDescription().getVersion()
                )
                .build());

        // Module Management
        setItem(28, new ItemBuilder(Material.COMPARATOR)
                .name("&d&lModule Management")
                .lore(
                        "&7View and toggle modules.",
                        "",
                        "&7Enabled modules:",
                        getModulesList(),
                        "",
                        "&7Edit modules.yml to change"
                )
                .build());

        // Back button
        setItem(31, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new SkyblockMenu(plugin));
        });
    }

    private String getModulesList() {
        StringBuilder sb = new StringBuilder();
        if (plugin.getModuleManager().isModuleEnabled("skills")) sb.append("&a- Skills\n");
        if (plugin.getModuleManager().isModuleEnabled("collections")) sb.append("&a- Collections\n");
        if (plugin.getModuleManager().isModuleEnabled("economy")) sb.append("&a- Economy\n");
        if (plugin.getModuleManager().isModuleEnabled("items")) sb.append("&a- Items\n");
        return sb.toString();
    }
}
