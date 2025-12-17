package com.skyblock.items;

import com.skyblock.items.abilities.ItemAbility;
import com.skyblock.items.rarity.Rarity;
import com.skyblock.items.stats.ItemStats;
import com.skyblock.items.stats.StatType;
import com.skyblock.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a custom Skyblock item with stats, abilities, and rarity.
 */
public class CustomItem {

    private final String id;
    private final String displayName;
    private final Material material;
    private final ItemCategory category;
    private final Rarity rarity;
    private final ItemStats stats;
    private final ItemAbility ability;
    private final boolean enchantGlow;
    private final String skullTexture;
    private final String leatherColor;

    // NBT keys
    public static final String NBT_ITEM_ID = "skyblock_item_id";
    public static final String NBT_RARITY = "skyblock_rarity";
    public static final String NBT_REFORGE = "skyblock_reforge";
    public static final String NBT_ENCHANTS = "skyblock_enchants";

    public CustomItem(String id, String displayName, Material material, ItemCategory category,
                      Rarity rarity, ItemStats stats, ItemAbility ability, boolean enchantGlow,
                      String skullTexture, String leatherColor) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.category = category;
        this.rarity = rarity;
        this.stats = stats != null ? stats : new ItemStats();
        this.ability = ability;
        this.enchantGlow = enchantGlow;
        this.skullTexture = skullTexture;
        this.leatherColor = leatherColor;
    }

    /**
     * Build the ItemStack for this custom item.
     */
    public ItemStack build(NamespacedKey itemIdKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Set display name
        meta.setDisplayName(ColorUtils.colorize(displayName));

        // Build lore
        List<String> lore = buildLore();
        meta.setLore(lore);

        // Add NBT data
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, id);

        // Add enchant glow
        if (enchantGlow) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.LUCK, 1);
            meta = item.getItemMeta();
        }

        // Hide attributes
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Build the lore for this item.
     */
    private List<String> buildLore() {
        List<String> lore = new ArrayList<>();

        // Add stats
        if (!stats.isEmpty()) {
            for (Map.Entry<StatType, Double> entry : stats.getStats().entrySet()) {
                if (entry.getValue() != 0) {
                    lore.add(ColorUtils.colorize(entry.getKey().format(entry.getValue())));
                }
            }
            lore.add("");
        }

        // Add ability
        if (ability != null) {
            lore.add(ColorUtils.colorize("&6Ability: " + ability.getName() + " &e&l" + ability.getTriggerDisplay()));
            if (ability.getDescription() != null) {
                for (String line : ability.getDescription()) {
                    lore.add(ColorUtils.colorize("&7" + line));
                }
            }
            if (ability.getManaCost() > 0) {
                lore.add(ColorUtils.colorize("&8Mana Cost: &3" + ability.getManaCost()));
            }
            if (ability.getCooldownSeconds() > 0) {
                lore.add(ColorUtils.colorize("&8Cooldown: &a" + ability.getCooldownSeconds() + "s"));
            }
            lore.add("");
        }

        // Add rarity line
        String categoryName = category != null ? category.getDisplayName() : "";
        lore.add(ColorUtils.colorize(rarity.getColorCode() + "&l" + rarity.getDisplayName() + " " + categoryName));

        return lore;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public ItemStats getStats() {
        return stats;
    }

    public ItemAbility getAbility() {
        return ability;
    }

    public boolean hasEnchantGlow() {
        return enchantGlow;
    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public String getLeatherColor() {
        return leatherColor;
    }

    /**
     * Check if this item has an ability.
     */
    public boolean hasAbility() {
        return ability != null;
    }

    /**
     * Check if this item is a weapon.
     */
    public boolean isWeapon() {
        return category == ItemCategory.WEAPON || category == ItemCategory.BOW;
    }

    /**
     * Check if this item is armor.
     */
    public boolean isArmor() {
        return category == ItemCategory.ARMOR;
    }

    /**
     * Check if this item is a tool.
     */
    public boolean isTool() {
        return category == ItemCategory.TOOL;
    }

    /**
     * Check if this item is an accessory.
     */
    public boolean isAccessory() {
        return category == ItemCategory.ACCESSORY;
    }

    /**
     * Builder class for CustomItem.
     */
    public static class Builder {
        private String id;
        private String displayName;
        private Material material = Material.STONE;
        private ItemCategory category = ItemCategory.MISC;
        private Rarity rarity = Rarity.COMMON;
        private ItemStats stats = new ItemStats();
        private ItemAbility ability;
        private boolean enchantGlow = false;
        private String skullTexture;
        private String leatherColor;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder material(String material) {
            try {
                this.material = Material.valueOf(material.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.material = Material.STONE;
            }
            return this;
        }

        public Builder category(ItemCategory category) {
            this.category = category;
            return this;
        }

        public Builder category(String category) {
            this.category = ItemCategory.fromString(category);
            return this;
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder rarity(String rarity) {
            this.rarity = Rarity.fromString(rarity);
            return this;
        }

        public Builder stats(ItemStats stats) {
            this.stats = stats;
            return this;
        }

        public Builder stat(StatType type, double value) {
            this.stats.setStat(type, value);
            return this;
        }

        public Builder ability(ItemAbility ability) {
            this.ability = ability;
            return this;
        }

        public Builder enchantGlow(boolean glow) {
            this.enchantGlow = glow;
            return this;
        }

        public Builder skullTexture(String texture) {
            this.skullTexture = texture;
            return this;
        }

        public Builder leatherColor(String color) {
            this.leatherColor = color;
            return this;
        }

        public CustomItem build() {
            return new CustomItem(id, displayName, material, category, rarity,
                    stats, ability, enchantGlow, skullTexture, leatherColor);
        }
    }
}
