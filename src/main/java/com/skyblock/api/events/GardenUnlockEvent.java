package com.skyblock.api.events;

import com.skyblock.garden.Garden;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player unlocks their garden.
 */
public class GardenUnlockEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Garden garden;

    public GardenUnlockEvent(Player player, Garden garden) {
        this.player = player;
        this.garden = garden;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Garden getGarden() {
        return garden;
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
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
