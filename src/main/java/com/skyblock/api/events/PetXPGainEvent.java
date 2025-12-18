package com.skyblock.api.events;

import com.skyblock.pets.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a pet gains XP.
 * Can be cancelled to prevent XP gain, or the amount can be modified.
 */
public class PetXPGainEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Pet pet;
    private final String source; // e.g., "MINING", "COMBAT"
    private double amount;

    public PetXPGainEvent(Player player, Pet pet, String source, double amount) {
        this.player = player;
        this.pet = pet;
        this.source = source;
        this.amount = amount;
    }

    public Player getPlayer() {
        return player;
    }

    public Pet getPet() {
        return pet;
    }

    /**
     * Get the source of the XP gain.
     * @return The skill type or source that granted the XP
     */
    public String getSource() {
        return source;
    }

    /**
     * Get the amount of XP being gained.
     * @return The XP amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Set the amount of XP to be gained.
     * Useful for multipliers or modifiers.
     * @param amount The new XP amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
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
