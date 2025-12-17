package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.menus.CollectionsMenu;
import com.skyblock.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Collections command handler.
 */
public class CollectionsCommand implements CommandExecutor {

    private final SkyblockPlugin plugin;

    public CollectionsCommand(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("skyblock.collections")) {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return true;
        }

        if (!plugin.getModuleManager().isModuleEnabled("collections")) {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.feature-disabled")));
            return true;
        }

        plugin.getGuiManager().openGUI(player, new CollectionsMenu(plugin));
        return true;
    }
}
