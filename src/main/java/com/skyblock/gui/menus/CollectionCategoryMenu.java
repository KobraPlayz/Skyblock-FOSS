package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.collections.Collection;
import com.skyblock.collections.CollectionCategory;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection category menu showing all collections in a category.
 */
public class CollectionCategoryMenu extends AbstractGUI {

    private final String categoryId;

    public CollectionCategoryMenu(SkyblockPlugin plugin, String categoryId) {
        super(plugin, "&8" + getCategoryName(plugin, categoryId) + " Collections", 6);
        this.categoryId = categoryId;
    }

    private static String getCategoryName(SkyblockPlugin plugin, String categoryId) {
        CollectionCategory category = plugin.getCollectionManager().getCategory(categoryId);
        return category != null ? ColorUtils.stripColor(ColorUtils.colorize(category.getDisplayName())) : categoryId;
    }

    @Override
    protected void build(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        fillBorder(createFiller());

        List<Collection> collections = plugin.getCollectionManager().getCollectionsByCategory(categoryId);
        int slot = 10;

        for (Collection collection : collections) {
            if (slot >= 44) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            long amount = sbPlayer != null ? sbPlayer.getCollectionAmount(collection.getId()) : 0;
            int tier = sbPlayer != null ? sbPlayer.getCollectionTier(collection.getId()) : 0;
            int maxTier = collection.getMaxTier();

            Material icon;
            try {
                icon = Material.valueOf(collection.getIcon().toUpperCase());
            } catch (IllegalArgumentException e) {
                icon = Material.STONE;
            }

            double progress = plugin.getCollectionManager().getProgressToNextTier(collection, amount, tier);
            long nextReq = plugin.getCollectionManager().getNextTierRequirement(collection, tier);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Tier: &e" + NumberUtils.intToRoman(tier) + "&7/&a" + NumberUtils.intToRoman(maxTier));
            lore.add("&7Collected: &e" + NumberUtils.format(amount));

            if (tier < maxTier && nextReq > 0) {
                lore.add("");
                lore.add("&7Progress to Tier " + NumberUtils.intToRoman(tier + 1) + ":");
                lore.add(createProgressBar(progress));
                lore.add("&7 " + NumberUtils.format(amount) + "&7/&a" + NumberUtils.format(nextReq) +
                        " &8(" + String.format("%.1f", progress) + "%)");
            } else if (tier >= maxTier) {
                lore.add("");
                lore.add("&6&lMAXED OUT!");
            }

            lore.add("");
            lore.add("&eClick for rewards!");

            setItem(slot, new ItemBuilder(icon)
                    .name(collection.getDisplayName())
                    .lore(lore)
                    .amount(Math.max(1, Math.min(tier, 64)))
                    .build(), event -> {
                // Show collection rewards
            });

            slot++;
        }

        // Back button
        setItem(48, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new CollectionsMenu(plugin));
        });

        // Close button
        setItem(50, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private String createProgressBar(double percentage) {
        int length = 20;
        int filled = (int) Math.round(percentage / 100 * length);

        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("-");
            } else {
                if (i == filled) bar.append("&f");
                bar.append("-");
            }
        }

        return ColorUtils.colorize(bar.toString());
    }
}
