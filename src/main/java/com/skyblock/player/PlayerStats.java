package com.skyblock.player;

/**
 * Represents calculated player stats from all sources.
 * Stats are calculated from skills, armor, weapons, pets, accessories, etc.
 */
public class PlayerStats {

    // Combat stats
    private double health;
    private double defense;
    private double trueDefense;
    private double strength;
    private double damage;
    private double critChance;
    private double critDamage;
    private double attackSpeed;
    private double ferocity;
    private double abilityDamage;

    // Utility stats
    private double speed;
    private double intelligence;
    private double magicFind;
    private double petLuck;
    private double seaCreatureChance;

    // Gathering stats
    private double miningSpeed;
    private double miningFortune;
    private double farmingFortune;
    private double foragingFortune;
    private double fishingSpeed;

    public PlayerStats() {
        // Initialize with default values
        this.health = 100;
        this.defense = 0;
        this.trueDefense = 0;
        this.strength = 0;
        this.damage = 0;
        this.critChance = 30;
        this.critDamage = 50;
        this.attackSpeed = 0;
        this.ferocity = 0;
        this.abilityDamage = 0;
        this.speed = 100;
        this.intelligence = 100;
        this.magicFind = 0;
        this.petLuck = 0;
        this.seaCreatureChance = 20;
        this.miningSpeed = 0;
        this.miningFortune = 0;
        this.farmingFortune = 0;
        this.foragingFortune = 0;
        this.fishingSpeed = 0;
    }

    // Health
    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void addHealth(double amount) {
        this.health += amount;
    }

    // Defense
    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public void addDefense(double amount) {
        this.defense += amount;
    }

    // True Defense
    public double getTrueDefense() {
        return trueDefense;
    }

    public void setTrueDefense(double trueDefense) {
        this.trueDefense = trueDefense;
    }

    public void addTrueDefense(double amount) {
        this.trueDefense += amount;
    }

    // Strength
    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public void addStrength(double amount) {
        this.strength += amount;
    }

    // Damage
    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void addDamage(double amount) {
        this.damage += amount;
    }

    // Crit Chance
    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }

    public void addCritChance(double amount) {
        this.critChance += amount;
    }

    // Crit Damage
    public double getCritDamage() {
        return critDamage;
    }

    public void setCritDamage(double critDamage) {
        this.critDamage = critDamage;
    }

    public void addCritDamage(double amount) {
        this.critDamage += amount;
    }

    // Attack Speed
    public double getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(double attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public void addAttackSpeed(double amount) {
        this.attackSpeed += amount;
    }

    // Ferocity
    public double getFerocity() {
        return ferocity;
    }

    public void setFerocity(double ferocity) {
        this.ferocity = ferocity;
    }

    public void addFerocity(double amount) {
        this.ferocity += amount;
    }

    // Ability Damage
    public double getAbilityDamage() {
        return abilityDamage;
    }

    public void setAbilityDamage(double abilityDamage) {
        this.abilityDamage = abilityDamage;
    }

    public void addAbilityDamage(double amount) {
        this.abilityDamage += amount;
    }

    // Speed
    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void addSpeed(double amount) {
        this.speed += amount;
    }

    // Intelligence
    public double getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(double intelligence) {
        this.intelligence = intelligence;
    }

    public void addIntelligence(double amount) {
        this.intelligence += amount;
    }

    // Magic Find
    public double getMagicFind() {
        return magicFind;
    }

    public void setMagicFind(double magicFind) {
        this.magicFind = magicFind;
    }

    public void addMagicFind(double amount) {
        this.magicFind += amount;
    }

    // Pet Luck
    public double getPetLuck() {
        return petLuck;
    }

    public void setPetLuck(double petLuck) {
        this.petLuck = petLuck;
    }

    public void addPetLuck(double amount) {
        this.petLuck += amount;
    }

    // Sea Creature Chance
    public double getSeaCreatureChance() {
        return seaCreatureChance;
    }

    public void setSeaCreatureChance(double seaCreatureChance) {
        this.seaCreatureChance = seaCreatureChance;
    }

    public void addSeaCreatureChance(double amount) {
        this.seaCreatureChance += amount;
    }

    // Mining Speed
    public double getMiningSpeed() {
        return miningSpeed;
    }

    public void setMiningSpeed(double miningSpeed) {
        this.miningSpeed = miningSpeed;
    }

    public void addMiningSpeed(double amount) {
        this.miningSpeed += amount;
    }

    // Mining Fortune
    public double getMiningFortune() {
        return miningFortune;
    }

    public void setMiningFortune(double miningFortune) {
        this.miningFortune = miningFortune;
    }

    public void addMiningFortune(double amount) {
        this.miningFortune += amount;
    }

    // Farming Fortune
    public double getFarmingFortune() {
        return farmingFortune;
    }

    public void setFarmingFortune(double farmingFortune) {
        this.farmingFortune = farmingFortune;
    }

    public void addFarmingFortune(double amount) {
        this.farmingFortune += amount;
    }

    // Foraging Fortune
    public double getForagingFortune() {
        return foragingFortune;
    }

    public void setForagingFortune(double foragingFortune) {
        this.foragingFortune = foragingFortune;
    }

    public void addForagingFortune(double amount) {
        this.foragingFortune += amount;
    }

    // Fishing Speed
    public double getFishingSpeed() {
        return fishingSpeed;
    }

    public void setFishingSpeed(double fishingSpeed) {
        this.fishingSpeed = fishingSpeed;
    }

    public void addFishingSpeed(double amount) {
        this.fishingSpeed += amount;
    }

    /**
     * Calculate effective health (health * defense multiplier).
     */
    public double getEffectiveHealth() {
        return health * (1 + defense / 100.0);
    }

    /**
     * Calculate damage reduction percentage.
     */
    public double getDamageReduction() {
        return defense / (defense + 100.0) * 100;
    }

    /**
     * Calculate expected damage with crit.
     * Formula: base_damage * (1 + strength/100) * (1 + crit_damage/100 * crit_chance/100)
     */
    public double getExpectedDamage(double baseDamage) {
        double strengthMultiplier = 1 + strength / 100.0;
        double critMultiplier = 1 + (critDamage / 100.0 * Math.min(critChance, 100) / 100.0);
        return baseDamage * strengthMultiplier * critMultiplier;
    }

    /**
     * Combine stats from another PlayerStats object.
     */
    public void combine(PlayerStats other) {
        this.health += other.health;
        this.defense += other.defense;
        this.trueDefense += other.trueDefense;
        this.strength += other.strength;
        this.damage += other.damage;
        this.critChance += other.critChance;
        this.critDamage += other.critDamage;
        this.attackSpeed += other.attackSpeed;
        this.ferocity += other.ferocity;
        this.abilityDamage += other.abilityDamage;
        this.speed += other.speed;
        this.intelligence += other.intelligence;
        this.magicFind += other.magicFind;
        this.petLuck += other.petLuck;
        this.seaCreatureChance += other.seaCreatureChance;
        this.miningSpeed += other.miningSpeed;
        this.miningFortune += other.miningFortune;
        this.farmingFortune += other.farmingFortune;
        this.foragingFortune += other.foragingFortune;
        this.fishingSpeed += other.fishingSpeed;
    }
}
