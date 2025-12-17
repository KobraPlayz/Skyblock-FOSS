package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.items.CustomItem;
import com.skyblock.items.ItemCategory;
import com.skyblock.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Item spawner menu for admins.
 */
public class ItemSpawnerMenu extends AbstractGUI {

    private int page;
    private final ItemCategory category;

    public ItemSpawnerMenu(SkyblockPlugin plugin) {
        this(plugin, null, 0);
    }

    public ItemSpawnerMenu(SkyblockPlugin plugin, ItemCategory category, int page) {
        super(plugin, "&8Item Spawner" + (category != null ? " - " + category.name() : ""), 6);
        this.category = category;
        this.page = page;
    }

    @Override
    protected void build(Player player) {
        if (!player.hasPermission("skyblock.admin.items")) {
            player.closeInventory();
            return;
        }

        fillBorder(createFiller());

        if (category == null) {
            // Show categories
            int slot = 10;
            for (ItemCategory cat : ItemCategory.values()) {
                if (slot >= 44) break;
                if (slot % 9 == 0) slot++;
                if (slot % 9 == 8) slot += 2;

                List<CustomItem> items = plugin.getItemManager().getItemsByCategory(cat);
                if (items.isEmpty()) continue;

                Material icon = getCategoryIcon(cat);

                setItem(slot, new ItemBuilder(icon)
                        .name("&e&l" + cat.name())
                        .lore(
                                "",
                                "&7Items: &a" + items.size(),
                                "",
                                "&eClick to view!"
                        )
                        .hideFlags()
                        .build(), event -> {
                    plugin.getGuiManager().openGUI(player, new ItemSpawnerMenu(plugin, cat, 0));
                });

                slot++;
            }
        } else {
            // Show items in category
            List<CustomItem> items = plugin.getItemManager().getItemsByCategory(category);
            int itemsPerPage = 36;
            int startIndex = page * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, items.size());

            int slot = 10;
            for (int i = startIndex; i < endIndex; i++) {
                if (slot >= 44) break;
                if (slot % 9 == 0) slot++;
                if (slot % 9 == 8) slot += 2;

                CustomItem customItem = items.get(i);
                ItemStack displayItem = customItem.build(
                        new org.bukkit.NamespacedKey(plugin, CustomItem.NBT_ITEM_ID));

                List<String> lore = new ArrayList<>();
                if (displayItem.hasItemMeta() && displayItem.getItemMeta().hasLore()) {
                    lore.addAll(displayItem.getItemMeta().getLore());
                }
                lore.add("");
                lore.add(ColorUtils.colorize("&eClick to receive!"));
                lore.add(ColorUtils.colorize("&eShift-click for stack!"));

                setItem(slot, new ItemBuilder(displayItem)
                        .lore(lore)
                        .build(), event -> {
                    int amount = event.getClick().isShiftClick() ? 64 : 1;
                    plugin.getItemManager().giveItem(player, customItem.getId(), amount);
                    playSuccessSound(player);
                    player.sendMessage(ColorUtils.colorize(
                            plugin.getConfigManager().getMessage("admin.item-given")
                                    .replace("{item}", customItem.getDisplayName())
                                    .replace("{player}", player.getName())));
                });

                slot++;
            }

            // Pagination
            int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

            if (page > 0) {
                setItem(48, new ItemBuilder(Material.ARROW)
                        .name("&c← Previous Page")
                        .lore("&7Page " + page + "/" + totalPages)
                        .build(), event -> {
                    plugin.getGuiManager().openGUI(player, new ItemSpawnerMenu(plugin, category, page - 1));
                });
            }

            if (page < totalPages - 1) {
                setItem(50, new ItemBuilder(Material.ARROW)
                        .name("&aNext Page →")
                        .lore("&7Page " + (page + 2) + "/" + totalPages)
                        .build(), event -> {
                    plugin.getGuiManager().openGUI(player, new ItemSpawnerMenu(plugin, category, page + 1));
                });
            }
        }

        // Back button
        setItem(category == null ? 49 : 45, createBackButton(), event -> {
            if (category == null) {
                plugin.getGuiManager().openGUI(player, new AdminMenu(plugin));
            } else {
                plugin.getGuiManager().openGUI(player, new ItemSpawnerMenu(plugin));
            }
        });
    }

    private Material getCategoryIcon(ItemCategory category) {
        switch (category) {
            case WEAPON: return Material.DIAMOND_SWORD;
            case BOW: return Material.BOW;
            case ARMOR:
            case HELMET: return Material.DIAMOND_HELMET;
            case CHESTPLATE: return Material.DIAMOND_CHESTPLATE;
            case LEGGINGS: return Material.DIAMOND_LEGGINGS;
            case BOOTS: return Material.DIAMOND_BOOTS;
            case TOOL:
            case PICKAXE: return Material.DIAMOND_PICKAXE;
            case AXE: return Material.DIAMOND_AXE;
            case HOE: return Material.DIAMOND_HOE;
            case ACCESSORY: return Material.EMERALD;
            case CONSUMABLE: return Material.GOLDEN_APPLE;
            case MATERIAL: return Material.DIAMOND;
            case MINION: return Material.ARMOR_STAND;
            default: return Material.STONE;
        }
    }
}
