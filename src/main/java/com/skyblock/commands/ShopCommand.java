package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Shop command handler.
 */
public class ShopCommand implements CommandExecutor {

    private final SkyblockPlugin plugin;

    public ShopCommand(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("skyblock.shop")) {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return true;
        }

        if (!plugin.getModuleManager().isSubModuleEnabled("economy", "shops")) {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.feature-disabled")));
            return true;
        }

        // TODO: Open shop menu
        player.sendMessage(ColorUtils.colorize("&7Shops are coming soon!"));

        return true;
    }
}
