package com.skyblock.economy;

import com.skyblock.SkyblockPlugin;
import com.skyblock.api.events.EconomyTransactionEvent;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Manages the economy system including coins and transactions.
 */
public class EconomyManager {

    private final SkyblockPlugin plugin;

    public EconomyManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get a player's purse balance.
     */
    public double getBalance(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        return sbPlayer != null ? sbPlayer.getPurse() : 0;
    }

    /**
     * Get a player's purse balance by SkyblockPlayer.
     */
    public double getBalance(SkyblockPlayer player) {
        return player != null ? player.getPurse() : 0;
    }

    /**
     * Check if a player has enough coins.
     */
    public boolean hasBalance(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * Add coins to a player's purse.
     */
    public boolean addCoins(Player player, double amount, String reason) {
        return addCoins(player, amount, reason, true);
    }

    /**
     * Add coins to a player's purse.
     */
    public boolean addCoins(Player player, double amount, String reason, boolean sendMessage) {
        if (amount <= 0) return false;

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return false;

        // Fire event
        EconomyTransactionEvent event = new EconomyTransactionEvent(
                player, amount, EconomyTransactionEvent.TransactionType.RECEIVE, reason
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        sbPlayer.addCoins(event.getAmount());

        // Log transaction
        logTransaction(sbPlayer, event.getAmount(), "RECEIVE", reason);

        // Send message
        if (sendMessage) {
            String message = plugin.getConfigManager().getRawMessage("economy.receive")
                    .replace("{amount}", NumberUtils.formatCoins(event.getAmount()));
            player.sendMessage(ColorUtils.colorize(message));
        }

        return true;
    }

    /**
     * Remove coins from a player's purse.
     */
    public boolean removeCoins(Player player, double amount, String reason) {
        return removeCoins(player, amount, reason, true);
    }

    /**
     * Remove coins from a player's purse.
     */
    public boolean removeCoins(Player player, double amount, String reason, boolean sendMessage) {
        if (amount <= 0) return false;

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return false;

        if (!sbPlayer.hasCoins(amount)) {
            if (sendMessage) {
                String message = plugin.getConfigManager().getRawMessage("economy.not-enough")
                        .replace("{required}", NumberUtils.formatCoins(amount));
                player.sendMessage(ColorUtils.colorize(message));
            }
            return false;
        }

        // Fire event
        EconomyTransactionEvent event = new EconomyTransactionEvent(
                player, amount, EconomyTransactionEvent.TransactionType.SPEND, reason
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        if (!sbPlayer.removeCoins(event.getAmount())) return false;

        // Log transaction
        logTransaction(sbPlayer, -event.getAmount(), "SPEND", reason);

        // Send message
        if (sendMessage) {
            String message = plugin.getConfigManager().getRawMessage("economy.spend")
                    .replace("{amount}", NumberUtils.formatCoins(event.getAmount()));
            player.sendMessage(ColorUtils.colorize(message));
        }

        return true;
    }

    /**
     * Set a player's purse balance.
     */
    public void setBalance(Player player, double amount) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return;

        double maxCoins = plugin.getConfigManager().getConfig().getDouble("economy.max-coins", 999999999999.0);
        amount = Math.max(0, Math.min(amount, maxCoins));

        sbPlayer.setPurse(amount);
    }

    /**
     * Transfer coins between players.
     */
    public boolean transferCoins(Player from, Player to, double amount, String reason) {
        if (amount <= 0) return false;
        if (!hasBalance(from, amount)) return false;

        if (removeCoins(from, amount, reason, false)) {
            addCoins(to, amount, reason, false);
            return true;
        }
        return false;
    }

    /**
     * Log a transaction to the database.
     */
    private void logTransaction(SkyblockPlayer player, double amount, String type, String description) {
        if (player.getActiveProfile() == null) return;

        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transactions (profile_id, amount, type, description, timestamp) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setInt(1, player.getActiveProfile().getId());
                stmt.setDouble(2, amount);
                stmt.setString(3, type);
                stmt.setString(4, description);
                stmt.setLong(5, System.currentTimeMillis());
                stmt.executeUpdate();
            }
        }).exceptionally(ex -> {
            plugin.log(Level.WARNING, "Failed to log transaction: " + ex.getMessage());
            return null;
        });
    }

    /**
     * Format coins for display.
     */
    public String formatCoins(double amount) {
        boolean useAbbreviations = plugin.getConfigManager().getConfig()
                .getBoolean("economy.format.use-abbreviations", true);

        if (useAbbreviations) {
            return NumberUtils.formatCoinsAbbreviated(amount);
        }
        return NumberUtils.formatCoins(amount);
    }

    /**
     * Get the coin symbol.
     */
    public String getCoinSymbol() {
        return plugin.getConfigManager().getConfig().getString("economy.format.symbol", "");
    }
}
