package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.menus.SkillsMenu;
import com.skyblock.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Skills command handler.
 */
public class SkillsCommand implements CommandExecutor {

    private final SkyblockPlugin plugin;

    public SkillsCommand(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("skyblock.skills")) {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return true;
        }

        if (!plugin.getModuleManager().isModuleEnabled("skills")) {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.feature-disabled")));
            return true;
        }

        plugin.getGuiManager().openGUI(player, new SkillsMenu(plugin));
        return true;
    }
}
