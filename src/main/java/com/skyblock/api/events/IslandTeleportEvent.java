package com.skyblock.api.events;

import com.skyblock.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player teleports to an island.
 */
public class IslandTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Island island;

    public IslandTeleportEvent(Player player, Island island) {
        this.player = player;
        this.island = island;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Island getIsland() {
        return island;
    }

    public boolean isOwner() {
        return island.isOwner(player.getUniqueId());
    }

    public boolean isMember() {
        return island.isMember(player.getUniqueId());
    }

    public boolean isVisitor() {
        return !island.isMember(player.getUniqueId());
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
