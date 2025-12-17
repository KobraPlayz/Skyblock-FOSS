package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.garden.CropType;
import com.skyblock.garden.Garden;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Garden Crop Upgrades menu (Desk).
 */
public class GardenUpgradesMenu extends AbstractGUI {

    private final UUID playerUuid;

    public GardenUpgradesMenu(SkyblockPlugin plugin, UUID playerUuid) {
        super(plugin, "&e&lDesk - Crop Upgrades", 5);
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

        // Info
        setItem(4, new ItemBuilder(Material.CARTOGRAPHY_TABLE)
                .name("&e&lCrop Upgrades")
                .lore(
                        "&7Upgrade your crops at the Desk!",
                        "",
                        "&7Each upgrade level grants:",
                        "&a+5 Farming Fortune",
                        "",
                        "&7Max Level: &e10"
                )
                .build());

        // Crop upgrades - positioned in a grid
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30};
        CropType[] crops = CropType.values();

        for (int i = 0; i < Math.min(crops.length, slots.length); i++) {
            CropType crop = crops[i];
            int level = garden.getCropUpgradeLevel(crop);
            long cost = crop.getUpgradeCost(level + 1);
            boolean canUpgrade = garden.getCopper() >= cost && level < 10;

            Material material = getCropMaterial(crop);

            setItem(slots[i], new ItemBuilder(material)
                    .name("&a" + formatCropName(crop) + " Upgrade")
                    .lore(
                            "&7Current Level: &e" + level + "/10",
                            "&7Farming Fortune: &a+" + (level * 5),
                            "",
                            level < 10 ? "&7Next Level Cost: &6" + NumberUtils.format(cost) + " Copper" : "&aMAX LEVEL!",
                            "",
                            canUpgrade ? "&eClick to upgrade!" : (level >= 10 ? "&cMax level reached!" : "&cNot enough Copper!")
                    )
                    .amount(Math.max(1, level))
                    .build(), event -> {
                if (level < 10 && garden.getCopper() >= cost) {
                    garden.setCopper(garden.getCopper() - cost);
                    garden.setCropUpgradeLevel(crop, level + 1);
                    plugin.getGardenManager().saveGarden(garden);
                    player.sendMessage("§a" + formatCropName(crop) + " upgraded to level " + (level + 1) + "!");
                    playSuccessSound(player);
                    plugin.getGuiManager().openGUI(player, new GardenUpgradesMenu(plugin, playerUuid));
                } else {
                    playErrorSound(player);
                }
            });
        }

        // Your Copper
        setItem(40, new ItemBuilder(Material.COPPER_INGOT)
                .name("&6Your Copper")
                .lore("&e" + NumberUtils.format(garden.getCopper()))
                .build());

        // Back button
        setItem(36, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenMenu(plugin, playerUuid));
        });

        // Close button
        setItem(44, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private Material getCropMaterial(CropType crop) {
        return switch (crop) {
            case WHEAT -> Material.WHEAT;
            case CARROT -> Material.CARROT;
            case POTATO -> Material.POTATO;
            case PUMPKIN -> Material.PUMPKIN;
            case MELON -> Material.MELON_SLICE;
            case COCOA_BEANS -> Material.COCOA_BEANS;
            case CACTUS -> Material.CACTUS;
            case SUGAR_CANE -> Material.SUGAR_CANE;
            case NETHER_WART -> Material.NETHER_WART;
            case MUSHROOM -> Material.RED_MUSHROOM;
        };
    }

    private String formatCropName(CropType crop) {
        String name = crop.name().replace("_", " ");
        StringBuilder formatted = new StringBuilder();
        for (String word : name.split(" ")) {
            formatted.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }
        return formatted.toString().trim();
    }
}
