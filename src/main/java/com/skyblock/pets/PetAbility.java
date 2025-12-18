package com.skyblock.pets;

import com.skyblock.player.PlayerStats;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface for pet abilities.
 * Pets can have passive and active abilities that grant bonuses.
 */
public interface PetAbility {

    /**
     * Get the name of the ability.
     */
    String getName();

    /**
     * Get the description of the ability at a specific pet level.
     *
     * @param petLevel The level of the pet
     * @return List of description lines
     */
    List<String> getDescription(int petLevel, PetRarity rarity);

    /**
     * Apply the ability's passive stats to the player.
     *
     * @param stats The player's stats to modify
     * @param petLevel The level of the pet
     * @param rarity The rarity of the pet
     */
    void applyStats(PlayerStats stats, int petLevel, PetRarity rarity);

    /**
     * Apply the ability's effects when pet is summoned.
     * Override if the ability needs to register listeners or do setup.
     *
     * @param player The player who summoned the pet
     * @param petLevel The level of the pet
     * @param rarity The rarity of the pet
     */
    default void onSummon(Player player, int petLevel, PetRarity rarity) {
        // Default: do nothing
    }

    /**
     * Remove the ability's effects when pet is unsummoned.
     *
     * @param player The player who unsummoned the pet
     */
    default void onUnsummon(Player player) {
        // Default: do nothing
    }

    /**
     * Check if this is a passive ability.
     */
    default boolean isPassive() {
        return true;
    }
}
