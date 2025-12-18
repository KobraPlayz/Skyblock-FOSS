package com.skyblock.api.events;

import com.skyblock.pets.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player summons a pet.
 * Can be cancelled to prevent summoning.
 */
public class PetSummonEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Pet pet;
    private final Pet previousPet; // May be null

    public PetSummonEvent(Player player, Pet pet, Pet previousPet) {
        this.player = player;
        this.pet = pet;
        this.previousPet = previousPet;
    }

    public Player getPlayer() {
        return player;
    }

    public Pet getPet() {
        return pet;
    }

    /**
     * Get the pet that was previously active.
     * @return The previous pet, or null if no pet was active
     */
    public Pet getPreviousPet() {
        return previousPet;
    }

    /**
     * Check if the player had a pet active before this summon.
     * @return true if a pet was active, false otherwise
     */
    public boolean hadPreviousPet() {
        return previousPet != null;
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
