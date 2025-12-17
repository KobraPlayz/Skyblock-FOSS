package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.menus.IslandSettingsMenu;
import com.skyblock.island.Island;
import com.skyblock.island.IslandManager;
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

/**
 * Island commands: /island, /is
 */
public class IslandCommand implements CommandExecutor, TabCompleter {

    private final SkyblockPlugin plugin;
    private final IslandManager islandManager;

    public IslandCommand(SkyblockPlugin plugin, IslandManager islandManager) {
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
            // Default: teleport to island
            teleportToIsland(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "home", "go", "tp" -> teleportToIsland(player);
            case "create" -> createIsland(player);
            case "sethome", "setspawn" -> setIslandSpawn(player);
            case "reset" -> resetIsland(player, args);
            case "settings" -> openSettings(player);
            case "invite" -> invitePlayer(player, args);
            case "kick" -> kickPlayer(player, args);
            case "ban" -> banPlayer(player, args);
            case "unban" -> unbanPlayer(player, args);
            case "visitors" -> listVisitors(player);
            case "public" -> togglePublic(player);
            case "pvp" -> togglePvp(player);
            case "help" -> sendHelp(player);
            default -> {
                player.sendMessage("§cUnknown subcommand. Use /island help");
            }
        }

        return true;
    }

    private void teleportToIsland(Player player) {
        player.sendMessage("§aTeleporting to your island...");
        islandManager.teleportToIsland(player);
    }

    private void createIsland(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) {
            player.sendMessage("§cYou don't have an active profile!");
            return;
        }

        islandManager.hasIsland(profile.getId()).thenAccept(hasIsland -> {
            if (hasIsland) {
                player.sendMessage("§cYou already have an island! Use /island reset to start over.");
                return;
            }

            player.sendMessage("§aCreating your island...");
            islandManager.createIsland(profile.getId(), player.getUniqueId()).thenAccept(island -> {
                if (island != null) {
                    player.sendMessage("§aIsland created! Teleporting...");
                    islandManager.teleportToIsland(player);
                } else {
                    player.sendMessage("§cFailed to create island!");
                }
            });
        });
    }

    private void setIslandSpawn(Player player) {
        islandManager.setIslandSpawn(player);
    }

    private void resetIsland(Player player, String[] args) {
        // Require confirmation
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            player.sendMessage("§c§lWARNING: §cThis will delete your entire island!");
            player.sendMessage("§cAll builds, chests, and items will be lost!");
            player.sendMessage("§eType §6/island reset confirm §eto proceed.");
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) {
            player.sendMessage("§cYou don't have an active profile!");
            return;
        }

        player.sendMessage("§cResetting your island...");
        islandManager.resetIsland(profile.getId(), player.getUniqueId()).thenAccept(success -> {
            if (success) {
                player.sendMessage("§aIsland reset! Use /island create to start fresh.");
                // Teleport to hub
                plugin.getWorldManager().teleportToHub(player);
            } else {
                player.sendMessage("§cFailed to reset island! You may not be the owner.");
            }
        });
    }

    private void openSettings(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) {
            player.sendMessage("§cYou don't have an active profile!");
            return;
        }

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            if (!island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou don't have permission to access island settings!");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                IslandSettingsMenu menu = new IslandSettingsMenu(plugin, player, island);
                plugin.getGuiManager().openGUI(player, menu);
            });
        });
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /island invite <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou can't invite yourself!");
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            if (!island.isMember(player.getUniqueId())) {
                player.sendMessage("§cOnly island members can invite players!");
                return;
            }

            // Check max members
            int maxMembers = plugin.getConfigManager().getIslandsConfig()
                .getInt("coop.max_members_default", 5);
            if (island.getMemberCount() >= maxMembers) {
                player.sendMessage("§cYour island has reached the maximum number of members!");
                return;
            }

            // Send invite (implemented in CoopManager)
            plugin.getCoopManager().sendInvite(player, target, island);
        });
    }

    private void kickPlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /island kick <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou can't kick yourself!");
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            if (!island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou must be an island member to kick players!");
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

            // Start kick vote
            plugin.getCoopManager().startKickVote(player, target, island);
        });
    }

    private void banPlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /island ban <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null || !island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou must be an island member to ban players!");
                return;
            }

            if (island.isMember(target.getUniqueId())) {
                player.sendMessage("§cYou can't ban an island member! Kick them first.");
                return;
            }

            island.banPlayer(target.getUniqueId());
            islandManager.saveIsland(island);
            player.sendMessage("§a" + target.getName() + " has been banned from your island!");

            // Teleport banned player away if on island
            if (target.getWorld().getName().equals(island.getWorldName())) {
                plugin.getWorldManager().teleportToHub(target);
                target.sendMessage("§cYou have been banned from this island!");
            }
        });
    }

    private void unbanPlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /island unban <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null || !island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou must be an island member!");
                return;
            }

            if (!island.isBanned(target.getUniqueId())) {
                player.sendMessage("§cThat player is not banned!");
                return;
            }

            island.unbanPlayer(target.getUniqueId());
            islandManager.saveIsland(island);
            player.sendMessage("§a" + target.getName() + " has been unbanned from your island!");
        });
    }

    private void listVisitors(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null) {
                player.sendMessage("§cYou don't have an island!");
                return;
            }

            player.sendMessage("§6§lIsland Visitors:");
            if (island.getCurrentVisitors().isEmpty()) {
                player.sendMessage("§7No visitors currently on your island.");
            } else {
                for (java.util.UUID visitorUuid : island.getCurrentVisitors()) {
                    Player visitor = Bukkit.getPlayer(visitorUuid);
                    if (visitor != null) {
                        player.sendMessage("§7- §a" + visitor.getName());
                    }
                }
            }
            player.sendMessage("§7Total visitors: §e" + island.getVisitorHistory().size());
        });
    }

    private void togglePublic(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null || !island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou must be an island member!");
                return;
            }

            island.setPublic(!island.isPublic());
            islandManager.saveIsland(island);

            if (island.isPublic()) {
                player.sendMessage("§aYour island is now §2PUBLIC§a! Anyone can visit with /visit " + player.getName());
            } else {
                player.sendMessage("§cYour island is now §4PRIVATE§c. Only invited players can visit.");
            }
        });
    }

    private void togglePvp(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getSkyblockPlayer(player);
        if (sbPlayer == null) return;

        PlayerProfile profile = sbPlayer.getActiveProfile();
        if (profile == null) return;

        islandManager.getIsland(profile.getId()).thenAccept(island -> {
            if (island == null || !island.isMember(player.getUniqueId())) {
                player.sendMessage("§cYou must be an island member!");
                return;
            }

            island.setPvpEnabled(!island.isPvpEnabled());
            islandManager.saveIsland(island);

            if (island.isPvpEnabled()) {
                player.sendMessage("§cPvP is now §4ENABLED §con your island!");
            } else {
                player.sendMessage("§aPvP is now §2DISABLED §aon your island.");
            }
        });
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lIsland Commands:");
        player.sendMessage("§e/island §7- Teleport to your island");
        player.sendMessage("§e/island create §7- Create a new island");
        player.sendMessage("§e/island sethome §7- Set island spawn point");
        player.sendMessage("§e/island settings §7- Open settings menu");
        player.sendMessage("§e/island invite <player> §7- Invite to co-op");
        player.sendMessage("§e/island kick <player> §7- Start kick vote");
        player.sendMessage("§e/island ban <player> §7- Ban from island");
        player.sendMessage("§e/island unban <player> §7- Unban player");
        player.sendMessage("§e/island visitors §7- List current visitors");
        player.sendMessage("§e/island public §7- Toggle public/private");
        player.sendMessage("§e/island pvp §7- Toggle PvP");
        player.sendMessage("§e/island reset confirm §7- Reset your island");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterByStart(Arrays.asList(
                "home", "create", "sethome", "settings", "invite", "kick",
                "ban", "unban", "visitors", "public", "pvp", "reset", "help"
            ), args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick") || sub.equals("ban") || sub.equals("unban")) {
                return null; // Return player names
            }
            if (sub.equals("reset")) {
                return filterByStart(List.of("confirm"), args[1]);
            }
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
