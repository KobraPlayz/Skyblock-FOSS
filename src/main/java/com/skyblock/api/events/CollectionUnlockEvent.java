package com.skyblock.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player unlocks a new collection tier.
 */
public class CollectionUnlockEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final String collectionId;
    private final int oldTier;
    private final int newTier;

    public CollectionUnlockEvent(Player player, String collectionId, int oldTier, int newTier) {
        this.player = player;
        this.collectionId = collectionId;
        this.oldTier = oldTier;
        this.newTier = newTier;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public int getOldTier() {
        return oldTier;
    }

    public int getNewTier() {
        return newTier;
    }

    public int getTiersGained() {
        return newTier - oldTier;
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
}
