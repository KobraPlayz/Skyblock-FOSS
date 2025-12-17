package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.garden.Garden;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Garden SkyMart shop menu.
 */
public class GardenShopMenu extends AbstractGUI {

    private final UUID playerUuid;

    public GardenShopMenu(SkyblockPlugin plugin, UUID playerUuid) {
        super(plugin, "&a&lSkyMart", 5);
        this.playerUuid = playerUuid;
    }

    @Override
    protected void build(Player player) {
        Garden garden = plugin.getGardenManager().getGarden(playerUuid);
        if (garden == null) {
            player.closeInventory();
            return;
        }

        fillBorder(createFiller());

        // Your Copper display
        setItem(4, new ItemBuilder(Material.COPPER_INGOT)
                .name("&6Your Copper")
                .lore(
                        "&e" + NumberUtils.format(garden.getCopper()),
                        "",
                        "&7Earn Copper by completing",
                        "&7visitor requests!"
                )
                .build());

        // Seeds section
        setItem(19, createShopItem(Material.WHEAT_SEEDS, "Wheat Seeds", 10, 16, garden), event -> {
            purchaseItem(player, garden, 10, new ItemStack(Material.WHEAT_SEEDS, 16), "Wheat Seeds x16");
        });

        setItem(20, createShopItem(Material.CARROT, "Carrot Seeds", 15, 16, garden), event -> {
            purchaseItem(player, garden, 15, new ItemStack(Material.CARROT, 16), "Carrot Seeds x16");
        });

        setItem(21, createShopItem(Material.POTATO, "Potato Seeds", 15, 16, garden), event -> {
            purchaseItem(player, garden, 15, new ItemStack(Material.POTATO, 16), "Potato Seeds x16");
        });

        setItem(22, createShopItem(Material.PUMPKIN_SEEDS, "Pumpkin Seeds", 25, 8, garden), event -> {
            purchaseItem(player, garden, 25, new ItemStack(Material.PUMPKIN_SEEDS, 8), "Pumpkin Seeds x8");
        });

        setItem(23, createShopItem(Material.MELON_SEEDS, "Melon Seeds", 25, 8, garden), event -> {
            purchaseItem(player, garden, 25, new ItemStack(Material.MELON_SEEDS, 8), "Melon Seeds x8");
        });

        setItem(24, createShopItem(Material.COCOA_BEANS, "Cocoa Beans", 30, 8, garden), event -> {
            purchaseItem(player, garden, 30, new ItemStack(Material.COCOA_BEANS, 8), "Cocoa Beans x8");
        });

        setItem(25, createShopItem(Material.NETHER_WART, "Nether Wart", 50, 8, garden), event -> {
            purchaseItem(player, garden, 50, new ItemStack(Material.NETHER_WART, 8), "Nether Wart x8");
        });

        // Tools section
        setItem(29, createShopItem(Material.DIAMOND_HOE, "Farming Hoe", 500, 1, garden), event -> {
            purchaseItem(player, garden, 500, new ItemStack(Material.DIAMOND_HOE, 1), "Diamond Hoe");
        });

        setItem(30, createShopItem(Material.BONE_MEAL, "Bone Meal", 5, 32, garden), event -> {
            purchaseItem(player, garden, 5, new ItemStack(Material.BONE_MEAL, 32), "Bone Meal x32");
        });

        setItem(31, createShopItem(Material.WATER_BUCKET, "Water Bucket", 50, 1, garden), event -> {
            purchaseItem(player, garden, 50, new ItemStack(Material.WATER_BUCKET, 1), "Water Bucket");
        });

        // Special items
        setItem(33, new ItemBuilder(Material.COMPOSTER)
                .name("&2Plot Unlock Token")
                .lore(
                        "&7Unlock an additional garden plot!",
                        "",
                        "&6Cost: &e1,000 Copper",
                        "",
                        garden.getCopper() >= 1000 ? "&eClick to purchase!" : "&cNot enough Copper!"
                )
                .build(), event -> {
            if (garden.getCopper() >= 1000) {
                garden.setCopper(garden.getCopper() - 1000);
                // Unlock next locked plot
                boolean unlocked = false;
                for (var plot : garden.getPlots()) {
                    if (plot.getStatus() == com.skyblock.garden.GardenPlot.Status.LOCKED) {
                        plot.setStatus(com.skyblock.garden.GardenPlot.Status.UNLOCKED);
                        unlocked = true;
                        break;
                    }
                }
                if (unlocked) {
                    plugin.getGardenManager().saveGarden(garden);
                    player.sendMessage("§aUnlocked a new garden plot!");
                    playSuccessSound(player);
                } else {
                    garden.setCopper(garden.getCopper() + 1000); // Refund
                    player.sendMessage("§cAll plots are already unlocked!");
                }
                plugin.getGuiManager().openGUI(player, new GardenShopMenu(plugin, playerUuid));
            } else {
                playErrorSound(player);
            }
        });

        setItem(34, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&aGarden XP Boost")
                .lore(
                        "&7Gain 500 Garden XP instantly!",
                        "",
                        "&6Cost: &e250 Copper",
                        "",
                        garden.getCopper() >= 250 ? "&eClick to purchase!" : "&cNot enough Copper!"
                )
                .build(), event -> {
            if (garden.getCopper() >= 250) {
                garden.setCopper(garden.getCopper() - 250);
                garden.setXp(garden.getXp() + 500);
                plugin.getGardenManager().saveGarden(garden);
                player.sendMessage("§aGained 500 Garden XP!");
                playSuccessSound(player);
                plugin.getGuiManager().openGUI(player, new GardenShopMenu(plugin, playerUuid));
            } else {
                playErrorSound(player);
            }
        });

        // Back button
        setItem(36, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenMenu(plugin, playerUuid));
        });

        // Close button
        setItem(44, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private ItemStack createShopItem(Material material, String name, long cost, int amount, Garden garden) {
        boolean canAfford = garden.getCopper() >= cost;
        return new ItemBuilder(material)
                .name("&a" + name)
                .lore(
                        "&7Amount: &e" + amount,
                        "",
                        "&6Cost: &e" + NumberUtils.format(cost) + " Copper",
                        "",
                        canAfford ? "&eClick to purchase!" : "&cNot enough Copper!"
                )
                .amount(Math.min(amount, 64))
                .build();
    }

    private void purchaseItem(Player player, Garden garden, long cost, ItemStack item, String itemName) {
        if (garden.getCopper() >= cost) {
            garden.setCopper(garden.getCopper() - cost);
            plugin.getGardenManager().saveGarden(garden);

            // Give item to player
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
                player.sendMessage("§aPurchased " + itemName + "!");
                playSuccessSound(player);
            } else {
                garden.setCopper(garden.getCopper() + cost); // Refund
                plugin.getGardenManager().saveGarden(garden);
                player.sendMessage("§cYour inventory is full!");
                playErrorSound(player);
            }
            plugin.getGuiManager().openGUI(player, new GardenShopMenu(plugin, playerUuid));
        } else {
            playErrorSound(player);
        }
    }
}
