package com.skyblock.pets.abilities;

import com.skyblock.items.stats.StatType;
import com.skyblock.pets.PetAbility;
import com.skyblock.pets.PetRarity;
import com.skyblock.player.PlayerStats;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Mining pet ability that grants mining speed and fortune.
 */
public class MiningPetAbility implements PetAbility {

    private final String name;
    private final double baseMiningSpeedPerLevel;
    private final double baseMiningFortunePerLevel;

    public MiningPetAbility(String name, double baseMiningSpeedPerLevel, double baseMiningFortunePerLevel) {
        this.name = name;
        this.baseMiningSpeedPerLevel = baseMiningSpeedPerLevel;
        this.baseMiningFortunePerLevel = baseMiningFortunePerLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription(int petLevel, PetRarity rarity) {
        List<String> description = new ArrayList<>();
        description.add("&7Grants mining bonuses:");

        double miningSpeed = calculateMiningSpeed(petLevel, rarity);
        double miningFortune = calculateMiningFortune(petLevel, rarity);

        description.add("&6+ " + String.format("%.1f", miningSpeed) + " " + StatType.MINING_SPEED.getSymbol() + " Mining Speed");
        description.add("&6+ " + String.format("%.1f", miningFortune) + " " + StatType.MINING_FORTUNE.getSymbol() + " Mining Fortune");

        return description;
    }

    @Override
    public void applyStats(PlayerStats stats, int petLevel, PetRarity rarity) {
        stats.addMiningSpeed(calculateMiningSpeed(petLevel, rarity));
        stats.addMiningFortune(calculateMiningFortune(petLevel, rarity));
    }

    private double calculateMiningSpeed(int petLevel, PetRarity rarity) {
        return baseMiningSpeedPerLevel * petLevel * rarity.getStatMultiplier();
    }

    private double calculateMiningFortune(int petLevel, PetRarity rarity) {
        return baseMiningFortunePerLevel * petLevel * 100 * rarity.getStatMultiplier();
    }
}
