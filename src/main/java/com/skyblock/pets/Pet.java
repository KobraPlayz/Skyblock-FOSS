package com.skyblock.pets;

import com.skyblock.pets.abilities.PetAbilityRegistry;
import com.skyblock.player.PlayerStats;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player's pet.
 * Pets provide stat bonuses and have unique abilities.
 */
public class Pet {

    private final int id;
    private final int profileId;
    private final PetType type;
    private PetRarity rarity;
    private int level;
    private double xp;
    private int candyUsed;
    private String heldItem;
    private boolean active;

    // Cached abilities
    private List<PetAbility> abilities;

    public Pet(int id, int profileId, PetType type, PetRarity rarity, int level, double xp, int candyUsed, String heldItem, boolean active) {
        this.id = id;
        this.profileId = profileId;
        this.type = type;
        this.rarity = rarity;
        this.level = level;
        this.xp = xp;
        this.candyUsed = candyUsed;
        this.heldItem = heldItem;
        this.active = active;
        this.abilities = PetAbilityRegistry.getAbilities(type);
    }

    /**
     * Create a new pet.
     */
    public Pet(int profileId, PetType type, PetRarity rarity) {
        this(0, profileId, type, rarity, 1, 0, 0, null, false);
    }

    public int getId() {
        return id;
    }

    public int getProfileId() {
        return profileId;
    }

    public PetType getType() {
        return type;
    }

    public PetRarity getRarity() {
        return rarity;
    }

    public void setRarity(PetRarity rarity) {
        this.rarity = rarity;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.min(level, type.getMaxLevel());
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public int getCandyUsed() {
        return candyUsed;
    }

    public void setCandyUsed(int candyUsed) {
        this.candyUsed = candyUsed;
    }

    public String getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(String heldItem) {
        this.heldItem = heldItem;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<PetAbility> getAbilities() {
        if (abilities == null) {
            abilities = PetAbilityRegistry.getAbilities(type);
        }
        return abilities;
    }

    /**
     * Get the XP required for the next level.
     */
    public double getXpForNextLevel() {
        return PetXPCalculator.getXPForNextLevel(level, rarity, type.getMaxLevel());
    }

    /**
     * Get the XP required for the current level.
     */
    public double getXpForCurrentLevel() {
        return PetXPCalculator.getXPForLevel(level, rarity);
    }

    /**
     * Get progress to next level as percentage.
     */
    public double getProgressToNextLevel() {
        return PetXPCalculator.getProgressToNextLevel(xp, level, rarity, type.getMaxLevel());
    }

    /**
     * Get XP remaining until next level.
     */
    public double getXpToNextLevel() {
        return PetXPCalculator.getXPToNextLevel(xp, level, rarity, type.getMaxLevel());
    }

    /**
     * Add XP to the pet.
     * Returns true if the pet leveled up.
     */
    public boolean addXp(double amount) {
        if (level >= type.getMaxLevel()) {
            return false;
        }

        int oldLevel = level;
        xp += amount;

        // Recalculate level based on total XP
        level = PetXPCalculator.calculateLevelFromXP(xp, rarity, type.getMaxLevel());

        return level > oldLevel;
    }

    /**
     * Get the stat bonuses this pet provides.
     */
    public PlayerStats getStatBonus() {
        PlayerStats stats = new PlayerStats();

        // Apply abilities' stat bonuses
        for (PetAbility ability : getAbilities()) {
            ability.applyStats(stats, level, rarity);
        }

        return stats;
    }

    /**
     * Get the display name of the pet.
     */
    public String getDisplayName() {
        return rarity.getColor() + "[Lvl " + level + "] " + type.getDisplayName();
    }

    /**
     * Calculate current level from XP.
     * Delegates to PetXPCalculator.
     */
    public static int calculateLevel(double totalXp, PetRarity rarity, int maxLevel) {
        return PetXPCalculator.calculateLevelFromXP(totalXp, rarity, maxLevel);
    }
}
