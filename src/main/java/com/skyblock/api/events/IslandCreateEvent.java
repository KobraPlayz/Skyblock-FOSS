package com.skyblock.api.events;

import com.skyblock.island.Island;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event fired when a player creates a new island.
 */
public class IslandCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final UUID playerUuid;
    private final Island island;

    public IslandCreateEvent(UUID playerUuid, Island island) {
        this.playerUuid = playerUuid;
        this.island = island;
        this.cancelled = false;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Island getIsland() {
        return island;
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
