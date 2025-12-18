package com.skyblock.pets.abilities;

import com.skyblock.pets.PetAbility;
import com.skyblock.pets.PetType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for pet abilities.
 * Maps pet types to their abilities.
 */
public class PetAbilityRegistry {

    private static final Map<PetType, List<PetAbility>> ABILITY_MAP = new EnumMap<>(PetType.class);

    static {
        registerAllAbilities();
    }

    private static void registerAllAbilities() {
        // Combat Pets
        register(PetType.WOLF, new CombatPetAbility("Pack Leader", 0.1, 0.005));
        register(PetType.TIGER, new CombatPetAbility("Ferocity", 0.15, 0.006));
        register(PetType.LION, new CombatPetAbility("King of Beasts", 0.2, 0.007));
        register(PetType.ENDERMAN, new CombatPetAbility("Ender Warp", 0.12, 0.005));
        register(PetType.ZOMBIE, new CombatPetAbility("Zombie Horde", 0.08, 0.004));
        register(PetType.SKELETON, new CombatPetAbility("Bone Arrows", 0.1, 0.005));
        register(PetType.SPIDER, new CombatPetAbility("Web Weaver", 0.09, 0.004));
        register(PetType.BLAZE, new CombatPetAbility("Inferno", 0.15, 0.006));
        register(PetType.PHOENIX, new CombatPetAbility("Rebirth", 0.18, 0.007));
        register(PetType.ENDER_DRAGON, new CombatPetAbility("Dragon Rage", 0.25, 0.01));

        // Mining Pets
        register(PetType.MITHRIL_GOLEM, new MiningPetAbility("Mithril Affinity", 0.2, 0.008));
        register(PetType.SILVERFISH, new MiningPetAbility("Quick Miner", 0.15, 0.006));
        register(PetType.BAL, new MiningPetAbility("Bal Power", 0.18, 0.007));
        register(PetType.ROCK, new MiningPetAbility("Rocky Strength", 0.12, 0.005));

        // Farming Pets
        register(PetType.BEE, new FarmingPetAbility("Pollination", 0.18, 0.007));
        register(PetType.RABBIT, new FarmingPetAbility("Lucky Foot", 0.15, 0.006));
        register(PetType.CHICKEN, new FarmingPetAbility("Egg Hatching", 0.12, 0.005));
        register(PetType.PIG, new FarmingPetAbility("Truffle Finder", 0.14, 0.006));
        register(PetType.MUSHROOM_COW, new FarmingPetAbility("Mycologist", 0.16, 0.007));
        register(PetType.ELEPHANT, new FarmingPetAbility("Trunk Storage", 0.2, 0.008));

        // Foraging Pets
        register(PetType.MONKEY, new ForagingPetAbility("Vine Swing", 0.15, 0.006));
        register(PetType.OCELOT, new ForagingPetAbility("Tree Hunter", 0.14, 0.006));
        register(PetType.PARROT, new ForagingPetAbility("Feather Friend", 0.13, 0.005));

        // Fishing Pets
        register(PetType.DOLPHIN, new FishingPetAbility("Dolphin Echolocation", 0.16, 0.007));
        register(PetType.SQUID, new FishingPetAbility("Ink Bomb", 0.12, 0.005));
        register(PetType.FLYING_FISH, new FishingPetAbility("Flying Speed", 0.14, 0.006));
        register(PetType.BLUE_WHALE, new FishingPetAbility("Ocean Authority", 0.18, 0.008));
        register(PetType.GUARDIAN, new FishingPetAbility("Laser Focus", 0.15, 0.006));

        // Enchanting Pets
        register(PetType.ENDERMITE, new EnchantingPetAbility("Mite Power", 0.15, 0.006));
        register(PetType.SHEEP, new EnchantingPetAbility("Wool Wisdom", 0.12, 0.005));

        // Alchemy Pets
        register(PetType.HORSE, new AlchemyPetAbility("Hoof Wisdom", 0.13, 0.005));
        register(PetType.GUARDIAN_PET, new AlchemyPetAbility("Potion Protection", 0.14, 0.006));

        // Special Pets
        register(PetType.GOLDEN_DRAGON, new CombatPetAbility("Golden Scales", 0.3, 0.012));
    }

    /**
     * Register an ability for a pet type.
     */
    private static void register(PetType type, PetAbility ability) {
        ABILITY_MAP.computeIfAbsent(type, k -> new ArrayList<>()).add(ability);
    }

    /**
     * Get abilities for a pet type.
     */
    public static List<PetAbility> getAbilities(PetType type) {
        return new ArrayList<>(ABILITY_MAP.getOrDefault(type, new ArrayList<>()));
    }

    /**
     * Check if a pet type has abilities registered.
     */
    public static boolean hasAbilities(PetType type) {
        return ABILITY_MAP.containsKey(type) && !ABILITY_MAP.get(type).isEmpty();
    }
}
