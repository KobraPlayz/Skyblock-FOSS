package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Hub command: /hub, /spawn
 */
public class HubCommand implements CommandExecutor {

    private final SkyblockPlugin plugin;

    public HubCommand(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        player.sendMessage("§aTeleporting to the Hub...");
        plugin.getWorldManager().teleportToHub(player);
        return true;
    }
}
