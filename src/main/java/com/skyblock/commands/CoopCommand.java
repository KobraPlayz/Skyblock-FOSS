package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.coop.CoopInvite;
import com.skyblock.coop.CoopManager;
import com.skyblock.island.Island;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Co-op commands: /coopadd, /coopkick, /coopview, /coopsalvage, /coopleave
 */
public class CoopCommand implements CommandExecutor, TabCompleter {

    private final SkyblockPlugin plugin;
    private final CoopManager coopManager;

    public CoopCommand(SkyblockPlugin plugin, CoopManager coopManager) {
        this.plugin = plugin;
        this.coopManager = coopManager;
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

        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "coopadd" -> handleCoopAdd(player, args);
            case "coopkick" -> handleCoopKick(player, args);
            case "coopview" -> handleCoopView(player);
            case "coopsalvage" -> handleCoopSalvage(player, args);
            case "coopleave" -> handleCoopLeave(player);
            default -> player.sendMessage("§cUnknown command!");
        }

        return true;
    }

    private void handleCoopAdd(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /coopadd <accept|deny|player>");
            return;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("accept")) {
            coopManager.acceptInvite(player);
            return;
        }

        if (arg.equals("deny")) {
            coopManager.denyInvite(player);
            return;
        }

        // Invite a player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) {
            player.sendMessage("§cYou don't have an active profile!");
            return;
        }

        plugin.getIslandManager().getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            if (!island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou must be an island member to invite players!");
                return;
            }

            coopManager.sendInvite(player, target, island);
        });
    }

    private void handleCoopKick(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cUsage: /coopkick <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        plugin.getIslandManager().getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            if (!island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou must be an island member!");
                return;
            }

            if (!island.isMember(target.getUniqueId())) {
                player.sendMessage("§cThat player is not an island member!");
                return;
            }

            if (island.isOwner(target.getUniqueId())) {
                player.sendMessage("§cYou can't kick the island owner!");
                return;
            }

            coopManager.startKickVote(player, target, island);
        });
    }

    private void handleCoopView(Player player) {
        // View pending invite
        CoopInvite invite = coopManager.getPendingInvite(player.getUniqueId());

        if (invite == null) {
            player.sendMessage("§7You have no pending co-op invites.");
            return;
        }

        if (invite.isExpired()) {
            player.sendMessage("§7Your pending invite has expired.");
            return;
        }

        Player inviter = Bukkit.getPlayer(invite.getInviterUuid());
        String inviterName = inviter != null ? inviter.getName() : invite.getInviterUuid().toString();

        long remaining = invite.getTimeRemaining() / 1000;
        int minutes = (int) (remaining / 60);
        int seconds = (int) (remaining % 60);

        player.sendMessage("§6§lPending Co-op Invite:");
        player.sendMessage("§7From: §e" + inviterName);
        player.sendMessage("§7Expires in: §e" + minutes + "m " + seconds + "s");
        player.sendMessage("§aUse §2/coopadd accept §ato join.");
        player.sendMessage("§cUse §4/coopadd deny §cto decline.");
    }

    private void handleCoopSalvage(Player player, String[] args) {
        // TODO: Implement salvage system
        // This allows collecting items from a kicked/left member's personal storage
        player.sendMessage("§eCo-op salvage system coming soon!");
    }

    private void handleCoopLeave(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) {
            player.sendMessage("§cYou don't have an active profile!");
            return;
        }

        plugin.getIslandManager().getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            if (!island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou're not a member of this island!");
                return;
            }

            if (island.isOwner(player.getUniqueId())) {
                player.sendMessage("§cYou can't leave an island you own!");
                player.sendMessage("§7Transfer ownership first or delete the island.");
                return;
            }

            coopManager.leaveIsland(player, island);
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("coopadd") && args.length == 1) {
            List<String> suggestions = new ArrayList<>(Arrays.asList("accept", "deny"));
            // Add online players
            for (Player p : Bukkit.getOnlinePlayers()) {
                suggestions.add(p.getName());
            }
            return filterByStart(suggestions, args[0]);
        }

        if (cmdName.equals("coopkick") && args.length == 1) {
            // Return co-op members
            return null; // Return player names
        }

        return List.of();
    }

    private List<String> filterByStart(List<String> options, String start) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(start.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }
}
