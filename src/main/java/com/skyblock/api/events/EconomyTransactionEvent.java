package com.skyblock.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an economy transaction occurs.
 */
public class EconomyTransactionEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private double amount;
    private final TransactionType type;
    private final String reason;

    public EconomyTransactionEvent(Player player, double amount, TransactionType type, String reason) {
        this.player = player;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum TransactionType {
        RECEIVE,
        SPEND,
        TRANSFER,
        SHOP_BUY,
        SHOP_SELL
    }
}
