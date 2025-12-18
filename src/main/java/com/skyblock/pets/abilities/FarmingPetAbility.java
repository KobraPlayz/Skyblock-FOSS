package com.skyblock.pets.abilities;

import com.skyblock.items.stats.StatType;
import com.skyblock.pets.PetAbility;
import com.skyblock.pets.PetRarity;
import com.skyblock.player.PlayerStats;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Farming pet ability that grants farming fortune and health.
 */
public class FarmingPetAbility implements PetAbility {

    private final String name;
    private final double baseFarmingFortunePerLevel;
    private final double baseHealthPerLevel;

    public FarmingPetAbility(String name, double baseFarmingFortunePerLevel, double baseHealthPerLevel) {
        this.name = name;
        this.baseFarmingFortunePerLevel = baseFarmingFortunePerLevel;
        this.baseHealthPerLevel = baseHealthPerLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription(int petLevel, PetRarity rarity) {
        List<String> description = new ArrayList<>();
        description.add("&7Grants farming bonuses:");

        double farmingFortune = calculateFarmingFortune(petLevel, rarity);
        double health = calculateHealth(petLevel, rarity);

        description.add("&6+ " + String.format("%.1f", farmingFortune) + " " + StatType.FARMING_FORTUNE.getSymbol() + " Farming Fortune");
        description.add("&c+ " + String.format("%.1f", health) + " " + StatType.HEALTH.getSymbol() + " Health");

        return description;
    }

    @Override
    public void applyStats(PlayerStats stats, int petLevel, PetRarity rarity) {
        stats.addFarmingFortune(calculateFarmingFortune(petLevel, rarity));
        stats.addHealth(calculateHealth(petLevel, rarity));
    }

    private double calculateFarmingFortune(int petLevel, PetRarity rarity) {
        return baseFarmingFortunePerLevel * petLevel * 100 * rarity.getStatMultiplier();
    }

    private double calculateHealth(int petLevel, PetRarity rarity) {
        return baseHealthPerLevel * petLevel * 100 * rarity.getStatMultiplier();
    }
}
