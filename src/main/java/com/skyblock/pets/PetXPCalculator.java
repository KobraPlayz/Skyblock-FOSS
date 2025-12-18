package com.skyblock.pets;

/**
 * Calculates Pet XP requirements based on Hypixel Skyblock's actual formulas.
 *
 * These formulas are exponential and vary by rarity:
 * - Common: 954 * e^(0.0927 * level)
 * - Uncommon: 1588 * e^(0.0934 * level)
 * - Rare: 2400 * e^(0.0943 * level)
 * - Epic: 3330 * e^(0.0952 * level)
 * - Legendary: 4908 * e^(0.0989 * level)
 *
 * Total XP for level 100:
 * - Common: ~6 million XP
 * - Uncommon: ~8.6 million XP
 * - Rare: ~12.6 million XP
 * - Epic: ~18.6 million XP
 * - Legendary: ~25 million XP
 *
 * Source: https://hypixel.net/threads/total-xp-required-for-pet-levels-1-100-all-rarities.2620458/
 */
public class PetXPCalculator {

    // Exponential formula coefficients per rarity
    private static final double COMMON_COEFFICIENT = 954.0;
    private static final double UNCOMMON_COEFFICIENT = 1588.0;
    private static final double RARE_COEFFICIENT = 2400.0;
    private static final double EPIC_COEFFICIENT = 3330.0;
    private static final double LEGENDARY_COEFFICIENT = 4908.0;
    private static final double MYTHIC_COEFFICIENT = 4908.0; // Same as Legendary

    // Exponential formula exponents per rarity
    private static final double COMMON_EXPONENT = 0.0927;
    private static final double UNCOMMON_EXPONENT = 0.0934;
    private static final double RARE_EXPONENT = 0.0943;
    private static final double EPIC_EXPONENT = 0.0952;
    private static final double LEGENDARY_EXPONENT = 0.0989;
    private static final double MYTHIC_EXPONENT = 0.0989; // Same as Legendary

    /**
     * Calculate the TOTAL XP required to reach a specific level.
     * This is cumulative XP, not XP per level.
     *
     * @param level The target level (1-100 or 1-200)
     * @param rarity The pet rarity
     * @return Total XP required to reach this level
     */
    public static double getXPForLevel(int level, PetRarity rarity) {
        if (level <= 1) {
            return 0;
        }

        double coefficient = getCoefficient(rarity);
        double exponent = getExponent(rarity);

        // Formula: coefficient * e^(exponent * level)
        return coefficient * Math.exp(exponent * level);
    }

    /**
     * Calculate the level from total XP.
     * This uses the inverse of the exponential formula.
     *
     * @param totalXP The total XP accumulated
     * @param rarity The pet rarity
     * @param maxLevel The maximum level for this pet
     * @return The current level
     */
    public static int calculateLevelFromXP(double totalXP, PetRarity rarity, int maxLevel) {
        if (totalXP <= 0) {
            return 1;
        }

        // Binary search for the level (more efficient than iterating)
        int low = 1;
        int high = maxLevel;
        int result = 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            double xpRequired = getXPForLevel(mid, rarity);

            if (xpRequired <= totalXP) {
                result = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return result;
    }

    /**
     * Get XP required for the next level.
     *
     * @param currentLevel The current level
     * @param rarity The pet rarity
     * @return XP required for next level
     */
    public static double getXPForNextLevel(int currentLevel, PetRarity rarity, int maxLevel) {
        if (currentLevel >= maxLevel) {
            return 0;
        }
        return getXPForLevel(currentLevel + 1, rarity);
    }

    /**
     * Get progress percentage to next level.
     *
     * @param currentXP Current total XP
     * @param currentLevel Current level
     * @param rarity Pet rarity
     * @param maxLevel Maximum level
     * @return Progress as percentage (0-100)
     */
    public static double getProgressToNextLevel(double currentXP, int currentLevel, PetRarity rarity, int maxLevel) {
        if (currentLevel >= maxLevel) {
            return 100.0;
        }

        double currentLevelXP = getXPForLevel(currentLevel, rarity);
        double nextLevelXP = getXPForLevel(currentLevel + 1, rarity);

        if (nextLevelXP <= currentLevelXP) {
            return 100.0;
        }

        double progress = ((currentXP - currentLevelXP) / (nextLevelXP - currentLevelXP)) * 100.0;
        return Math.max(0.0, Math.min(100.0, progress));
    }

    /**
     * Get XP remaining until next level.
     *
     * @param currentXP Current total XP
     * @param currentLevel Current level
     * @param rarity Pet rarity
     * @param maxLevel Maximum level
     * @return XP remaining
     */
    public static double getXPToNextLevel(double currentXP, int currentLevel, PetRarity rarity, int maxLevel) {
        if (currentLevel >= maxLevel) {
            return 0;
        }

        double nextLevelXP = getXPForLevel(currentLevel + 1, rarity);
        return Math.max(0, nextLevelXP - currentXP);
    }

    /**
     * Get the coefficient for a rarity's XP formula.
     */
    private static double getCoefficient(PetRarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON_COEFFICIENT;
            case UNCOMMON -> UNCOMMON_COEFFICIENT;
            case RARE -> RARE_COEFFICIENT;
            case EPIC -> EPIC_COEFFICIENT;
            case LEGENDARY -> LEGENDARY_COEFFICIENT;
            case MYTHIC -> MYTHIC_COEFFICIENT;
        };
    }

    /**
     * Get the exponent for a rarity's XP formula.
     */
    private static double getExponent(PetRarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON_EXPONENT;
            case UNCOMMON -> UNCOMMON_EXPONENT;
            case RARE -> RARE_EXPONENT;
            case EPIC -> EPIC_EXPONENT;
            case LEGENDARY -> LEGENDARY_EXPONENT;
            case MYTHIC -> MYTHIC_EXPONENT;
        };
    }

    /**
     * Get the total XP required to max out a pet (level 100).
     * Useful for displaying max XP in GUIs.
     *
     * @param rarity Pet rarity
     * @return Total XP for level 100
     */
    public static double getMaxXP(PetRarity rarity) {
        return getXPForLevel(100, rarity);
    }

    /**
     * Get the total XP required to max out a pet (level 200 for special pets).
     *
     * @param rarity Pet rarity
     * @return Total XP for level 200
     */
    public static double getMaxXPLevel200(PetRarity rarity) {
        return getXPForLevel(200, rarity);
    }
}
