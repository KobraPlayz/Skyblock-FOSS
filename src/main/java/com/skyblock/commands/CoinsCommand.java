package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Coins command handler.
 */
public class CoinsCommand implements CommandExecutor {

    private final SkyblockPlugin plugin;

    public CoinsCommand(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("skyblock.coins")) {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return true;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return true;

        double coins = sbPlayer.getPurse();
        player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("economy.balance")
                .replace("{amount}", NumberUtils.formatCoins(coins))));

        return true;
    }
}
