package com.skyblock.pets;

import com.skyblock.skills.SkillType;
import org.bukkit.Material;

/**
 * Enum representing all pet types.
 * Each pet type corresponds to different activities and provides unique bonuses.
 */
public enum PetType {
    // Combat Pets
    WOLF("Wolf", Material.BONE, PetCategory.COMBAT, SkillType.COMBAT),
    TIGER("Tiger", Material.LEATHER, PetCategory.COMBAT, SkillType.COMBAT),
    LION("Lion", Material.LEATHER, PetCategory.COMBAT, SkillType.COMBAT),
    ENDERMAN("Enderman", Material.ENDER_PEARL, PetCategory.COMBAT, SkillType.COMBAT),
    ZOMBIE("Zombie", Material.ROTTEN_FLESH, PetCategory.COMBAT, SkillType.COMBAT),
    SKELETON("Skeleton", Material.BONE, PetCategory.COMBAT, SkillType.COMBAT),
    SPIDER("Spider", Material.SPIDER_EYE, PetCategory.COMBAT, SkillType.COMBAT),
    BLAZE("Blaze", Material.BLAZE_ROD, PetCategory.COMBAT, SkillType.COMBAT),
    PHOENIX("Phoenix", Material.FIRE_CHARGE, PetCategory.COMBAT, SkillType.COMBAT),
    ENDER_DRAGON("Ender Dragon", Material.DRAGON_EGG, PetCategory.COMBAT, SkillType.COMBAT),

    // Mining Pets
    MITHRIL_GOLEM("Mithril Golem", Material.PRISMARINE_CRYSTALS, PetCategory.MINING, SkillType.MINING),
    SILVERFISH("Silverfish", Material.IRON_ORE, PetCategory.MINING, SkillType.MINING),
    BAL("Bal", Material.MAGMA_BLOCK, PetCategory.MINING, SkillType.MINING),
    ROCK("Rock", Material.COBBLESTONE, PetCategory.MINING, SkillType.MINING),

    // Farming Pets
    BEE("Bee", Material.HONEYCOMB, PetCategory.FARMING, SkillType.FARMING),
    RABBIT("Rabbit", Material.RABBIT_FOOT, PetCategory.FARMING, SkillType.FARMING),
    CHICKEN("Chicken", Material.EGG, PetCategory.FARMING, SkillType.FARMING),
    PIG("Pig", Material.PORKCHOP, PetCategory.FARMING, SkillType.FARMING),
    MUSHROOM_COW("Mushroom Cow", Material.RED_MUSHROOM, PetCategory.FARMING, SkillType.FARMING),
    ELEPHANT("Elephant", Material.HAY_BLOCK, PetCategory.FARMING, SkillType.FARMING),

    // Foraging Pets
    MONKEY("Monkey", Material.JUNGLE_LOG, PetCategory.FORAGING, SkillType.FORAGING),
    OCELOT("Ocelot", Material.OAK_LOG, PetCategory.FORAGING, SkillType.FORAGING),
    PARROT("Parrot", Material.FEATHER, PetCategory.FORAGING, SkillType.FORAGING),

    // Fishing Pets
    DOLPHIN("Dolphin", Material.RAW_FISH, PetCategory.FISHING, SkillType.FISHING),
    SQUID("Squid", Material.INK_SAC, PetCategory.FISHING, SkillType.FISHING),
    FLYING_FISH("Flying Fish", Material.TROPICAL_FISH, PetCategory.FISHING, SkillType.FISHING),
    BLUE_WHALE("Blue Whale", Material.PRISMARINE_SHARD, PetCategory.FISHING, SkillType.FISHING),
    GUARDIAN("Guardian", Material.PRISMARINE_CRYSTALS, PetCategory.FISHING, SkillType.FISHING),

    // Enchanting Pets
    ENDERMITE("Endermite", Material.ENDER_EYE, PetCategory.ENCHANTING, SkillType.ENCHANTING),
    SHEEP("Sheep", Material.WHITE_WOOL, PetCategory.ENCHANTING, SkillType.ENCHANTING),

    // Alchemy Pets
    HORSE("Horse", Material.SADDLE, PetCategory.ALCHEMY, SkillType.ALCHEMY),
    GUARDIAN_PET("Guardian Pet", Material.SEA_LANTERN, PetCategory.ALCHEMY, SkillType.ALCHEMY),

    // Special Pets
    ENDER_PEARL("Ender Pearl", Material.ENDER_PEARL, PetCategory.SPECIAL, null),
    GOLDEN_DRAGON("Golden Dragon", Material.GOLD_BLOCK, PetCategory.SPECIAL, null),
    GHOUL("Ghoul", Material.SOUL_SAND, PetCategory.SPECIAL, null);

    private final String displayName;
    private final Material material;
    private final PetCategory category;
    private final SkillType relatedSkill;

    PetType(String displayName, Material material, PetCategory category, SkillType relatedSkill) {
        this.displayName = displayName;
        this.material = material;
        this.category = category;
        this.relatedSkill = relatedSkill;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public PetCategory getCategory() {
        return category;
    }

    public SkillType getRelatedSkill() {
        return relatedSkill;
    }

    /**
     * Get the max level for this pet type.
     * Most pets go to 100, special pets go to 200.
     */
    public int getMaxLevel() {
        return category == PetCategory.SPECIAL ? 200 : 100;
    }

    /**
     * Get pet type from skill.
     * Used to determine which pet should gain XP.
     */
    public static PetType fromSkill(SkillType skill) {
        // This will return the first pet that matches the skill
        for (PetType type : values()) {
            if (type.relatedSkill == skill) {
                return type;
            }
        }
        return null;
    }

    /**
     * Parse pet type from string.
     */
    public static PetType fromString(String name) {
        for (PetType type : values()) {
            if (type.name().equalsIgnoreCase(name.replace(" ", "_")) ||
                    type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Pet categories for organization.
     */
    public enum PetCategory {
        COMBAT,
        MINING,
        FARMING,
        FORAGING,
        FISHING,
        ENCHANTING,
        ALCHEMY,
        SPECIAL
    }
}
