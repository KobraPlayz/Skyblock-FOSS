package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.collections.CollectionCategory;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Collections categories menu GUI.
 */
public class CollectionsMenu extends AbstractGUI {

    public CollectionsMenu(SkyblockPlugin plugin) {
        super(plugin, "&8Collections", 4);
    }

    @Override
    protected void build(Player player) {
        fillBorder(createFiller());

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int slotIndex = 0;

        for (CollectionCategory category : plugin.getCollectionManager().getAllCategories()) {
            if (slotIndex >= slots.length) break;

            if (!plugin.getModuleManager().isSubModuleEnabled("collections", category.getId())) {
                continue;
            }

            Material icon;
            try {
                icon = Material.valueOf(category.getIcon().toUpperCase());
            } catch (IllegalArgumentException e) {
                icon = Material.STONE;
            }

            int slot = slots[slotIndex++];
            String categoryId = category.getId();

            setItem(slot, new ItemBuilder(icon)
                    .name(category.getDisplayName())
                    .lore(
                            "",
                            "&7" + category.getDescription(),
                            "",
                            "&eClick to view!"
                    )
                    .build(), event -> {
                plugin.getGuiManager().openGUI(player, new CollectionCategoryMenu(plugin, categoryId));
            });
        }

        // Back button
        setItem(31, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new SkyblockMenu(plugin));
        });
    }
}
