package com.skyblock.pets.abilities;

import com.skyblock.items.stats.StatType;
import com.skyblock.pets.PetAbility;
import com.skyblock.pets.PetRarity;
import com.skyblock.player.PlayerStats;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Combat pet ability that grants strength and crit damage.
 */
public class CombatPetAbility implements PetAbility {

    private final String name;
    private final double baseStrengthPerLevel;
    private final double baseCritDamagePerLevel;

    public CombatPetAbility(String name, double baseStrengthPerLevel, double baseCritDamagePerLevel) {
        this.name = name;
        this.baseStrengthPerLevel = baseStrengthPerLevel;
        this.baseCritDamagePerLevel = baseCritDamagePerLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription(int petLevel, PetRarity rarity) {
        List<String> description = new ArrayList<>();
        description.add("&7Grants combat bonuses:");

        double strength = calculateStrength(petLevel, rarity);
        double critDamage = calculateCritDamage(petLevel, rarity);

        description.add("&c+ " + String.format("%.1f", strength) + " " + StatType.STRENGTH.getSymbol() + " Strength");
        description.add("&9+ " + String.format("%.1f", critDamage) + "% " + StatType.CRIT_DAMAGE.getSymbol() + " Crit Damage");

        return description;
    }

    @Override
    public void applyStats(PlayerStats stats, int petLevel, PetRarity rarity) {
        stats.addStrength(calculateStrength(petLevel, rarity));
        stats.addCritDamage(calculateCritDamage(petLevel, rarity));
    }

    private double calculateStrength(int petLevel, PetRarity rarity) {
        return baseStrengthPerLevel * petLevel * rarity.getStatMultiplier();
    }

    private double calculateCritDamage(int petLevel, PetRarity rarity) {
        return baseCritDamagePerLevel * petLevel * 100 * rarity.getStatMultiplier();
    }
}
