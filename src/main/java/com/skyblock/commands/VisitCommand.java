package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.island.Island;
import com.skyblock.island.IslandManager;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Visit command: /visit <player>
 */
public class VisitCommand implements CommandExecutor, TabCompleter {

    private final SkyblockPlugin plugin;
    private final IslandManager islandManager;

    public VisitCommand(SkyblockPlugin plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!plugin.getModuleManager().isModuleEnabled("islands")) {
            player.sendMessage("§cIslands are currently disabled!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /visit <player>");
            return true;
        }

        String targetName = args[0];

        // First try online player
        Player targetOnline = Bukkit.getPlayer(targetName);
        if (targetOnline != null) {
            visitPlayer(player, targetOnline);
            return true;
        }

        // Try offline player
        @SuppressWarnings("deprecation")
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
        if (targetOffline.hasPlayedBefore()) {
            visitOfflinePlayer(player, targetOffline);
            return true;
        }

        player.sendMessage("§cPlayer not found!");
        return true;
    }

    private void visitPlayer(Player visitor, Player owner) {
        if (visitor.equals(owner)) {
            // Visit own island
            islandManager.teleportToIsland(visitor);
            return;
        }

        islandManager.visitIsland(visitor, owner);
    }

    private void visitOfflinePlayer(Player visitor, OfflinePlayer owner) {
        // Need to find their island without them being online
        // This is a simplified version - in production you'd want async database lookup

        SkyblockPlayer ownerSb = plugin.getPlayerManager().loadOfflinePlayer(owner.getUniqueId());
        if (ownerSb == null) {
            visitor.sendMessage("§cThat player doesn't have any Skyblock data!");
            return;
        }

        PlayerProfile profile = ownerSb.getActiveProfile();
        if (profile == null) {
            visitor.sendMessage("§cThat player doesn't have an active profile!");
            return;
        }

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                visitor.sendMessage("§cThat player doesn't have an island!");
                return;
            }

            // Check if public
            if (!island.isPublic()) {
                visitor.sendMessage("§cThat player's island is private!");
                return;
            }

            // Check if banned
            if (island.isBanned(visitor.getUniqueId())) {
                visitor.sendMessage("§cYou are banned from that island!");
                return;
            }

            // Check guest limit
            if (!island.canAcceptVisitors()) {
                visitor.sendMessage("§cThat island has reached its visitor limit!");
                return;
            }

            // Teleport
            visitor.sendMessage("§aVisiting §e" + owner.getName() + "§a's island...");

            plugin.getWorldManager().loadIslandWorld(island.getWorldName()).thenAccept(world -> {
                if (world == null) {
                    visitor.sendMessage("§cFailed to load the island!");
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    visitor.teleport(island.getSpawnLocation(world));
                    island.addVisitor(visitor.getUniqueId());
                });
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // Return online players with public islands
            List<String> suggestions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    suggestions.add(player.getName());
                }
            }
            return suggestions;
        }
        return List.of();
    }
}
