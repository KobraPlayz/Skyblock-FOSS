package com.skyblock.commands;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.menus.AdminMenu;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
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
 * Admin command handler.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final SkyblockPlugin plugin;

    public AdminCommand(SkyblockPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("sbadmin").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skyblock.admin")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getGuiManager().openGUI((Player) sender, new AdminMenu(plugin));
            } else {
                sendHelp(sender);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "coins":
                handleCoins(sender, args);
                break;
            case "skill":
                handleSkill(sender, args);
                break;
            case "collection":
                handleCollection(sender, args);
                break;
            case "give":
                handleGive(sender, args);
                break;
            default:
                sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&6&lSkyblockFOSS Admin Commands:"));
        sender.sendMessage(ColorUtils.colorize("&e/sbadmin reload &7- Reload configuration"));
        sender.sendMessage(ColorUtils.colorize("&e/sbadmin coins <player> <give|take|set> <amount> &7- Manage coins"));
        sender.sendMessage(ColorUtils.colorize("&e/sbadmin skill <player> <skill> <level> &7- Set skill level"));
        sender.sendMessage(ColorUtils.colorize("&e/sbadmin collection <player> <collection> <amount> &7- Set collection"));
        sender.sendMessage(ColorUtils.colorize("&e/sbadmin give <player> <item> [amount] &7- Give custom item"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("skyblock.admin.reload")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }

        plugin.reload();
        sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.reload-success")));
    }

    private void handleCoins(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyblock.admin.economy")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /sbadmin coins <player> <give|take|set> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-not-found")
                    .replace("{player}", args[1])));
            return;
        }

        double amount = NumberUtils.parseDouble(args[3], -1);
        if (amount < 0) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.invalid-number")
                    .replace("{input}", args[3])));
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(target);
        if (sbPlayer == null) return;

        switch (args[2].toLowerCase()) {
            case "give":
                sbPlayer.addCoins(amount);
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.coins-given")
                        .replace("{amount}", NumberUtils.formatCoins(amount))
                        .replace("{player}", target.getName())));
                break;
            case "take":
                sbPlayer.removeCoins(amount);
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.coins-taken")
                        .replace("{amount}", NumberUtils.formatCoins(amount))
                        .replace("{player}", target.getName())));
                break;
            case "set":
                sbPlayer.setPurse(amount);
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.coins-set")
                        .replace("{amount}", NumberUtils.formatCoins(amount))
                        .replace("{player}", target.getName())));
                break;
            default:
                sender.sendMessage(ColorUtils.colorize("&cUsage: /sbadmin coins <player> <give|take|set> <amount>"));
        }
    }

    private void handleSkill(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyblock.admin.players")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /sbadmin skill <player> <skill> <level>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-not-found")
                    .replace("{player}", args[1])));
            return;
        }

        String skill = args[2].toLowerCase();
        int level = NumberUtils.parseInt(args[3], -1);

        if (level < 0 || level > 60) {
            sender.sendMessage(ColorUtils.colorize("&cLevel must be between 0 and 60!"));
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(target);
        if (sbPlayer == null || sbPlayer.getActiveProfile() == null) return;

        long xp = plugin.getSkillManager().getXpForLevel(level);
        sbPlayer.getActiveProfile().setSkillData(skill, xp, level);
        sbPlayer.invalidateStatsCache();

        sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.skill-set")
                .replace("{player}", target.getName())
                .replace("{skill}", skill)
                .replace("{level}", String.valueOf(level))));
    }

    private void handleCollection(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyblock.admin.players")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /sbadmin collection <player> <collection> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-not-found")
                    .replace("{player}", args[1])));
            return;
        }

        String collection = args[2].toLowerCase();
        long amount = NumberUtils.parseLong(args[3], -1);

        if (amount < 0) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.invalid-number")
                    .replace("{input}", args[3])));
            return;
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(target);
        if (sbPlayer != null) {
            sbPlayer.addCollection(collection, amount);
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.collection-set")
                    .replace("{player}", target.getName())
                    .replace("{collection}", collection)
                    .replace("{amount}", String.valueOf(amount))));
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyblock.admin.items")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /sbadmin give <player> <item> [amount]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("general.player-not-found")
                    .replace("{player}", args[1])));
            return;
        }

        String itemId = args[2].toUpperCase();
        int amount = args.length > 3 ? NumberUtils.parseInt(args[3], 1) : 1;

        if (plugin.getItemManager().getItem(itemId) == null) {
            sender.sendMessage(ColorUtils.colorize("&cItem not found: " + itemId));
            return;
        }

        plugin.getItemManager().giveItem(target, itemId, amount);
        sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("admin.item-given")
                .replace("{item}", itemId)
                .replace("{player}", target.getName())));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "coins", "skill", "collection", "give"));
        } else if (args.length == 2) {
            // Player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "coins":
                    completions.addAll(Arrays.asList("give", "take", "set"));
                    break;
                case "skill":
                    for (com.skyblock.skills.SkillType skill : com.skyblock.skills.SkillType.values()) {
                        completions.add(skill.getConfigKey());
                    }
                    break;
                case "give":
                    completions.addAll(plugin.getItemManager().getItemIds());
                    break;
            }
        }

        return completions;
    }
}
