package com.skyblock.api.events;

import com.skyblock.player.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player switches profiles.
 */
public class ProfileSwitchEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PlayerProfile oldProfile;
    private final PlayerProfile newProfile;

    public ProfileSwitchEvent(Player player, PlayerProfile oldProfile, PlayerProfile newProfile) {
        this.player = player;
        this.oldProfile = oldProfile;
        this.newProfile = newProfile;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerProfile getOldProfile() {
        return oldProfile;
    }

    public PlayerProfile getNewProfile() {
        return newProfile;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
