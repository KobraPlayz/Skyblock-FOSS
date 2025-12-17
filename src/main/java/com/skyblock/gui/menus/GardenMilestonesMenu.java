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
 * Garden Crop Milestones menu.
 */
public class GardenMilestonesMenu extends AbstractGUI {

    private final UUID playerUuid;

    public GardenMilestonesMenu(SkyblockPlugin plugin, UUID playerUuid) {
        super(plugin, "&6&lCrop Milestones", 5);
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
        setItem(4, new ItemBuilder(Material.GOLDEN_CARROT)
                .name("&6&lCrop Milestones")
                .lore(
                        "&7Track your farming progress!",
                        "",
                        "&7Harvest crops to reach milestones",
                        "&7and earn rewards.",
                        "",
                        "&7Milestone rewards:",
                        "&a+Farming Fortune",
                        "&6+Copper",
                        "&d+Garden XP"
                )
                .build());

        // Crop milestones - positioned in a grid
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30};
        CropType[] crops = CropType.values();

        for (int i = 0; i < Math.min(crops.length, slots.length); i++) {
            CropType crop = crops[i];
            Garden.CropMilestone milestone = garden.getMilestone(crop);

            long harvested = milestone != null ? milestone.getTotalHarvested() : 0;
            int milestoneLevel = milestone != null ? milestone.getMilestoneLevel() : 0;
            long nextMilestone = getNextMilestoneRequirement(milestoneLevel);

            Material material = getCropMaterial(crop);
            double progress = nextMilestone > 0 ? (double) harvested / nextMilestone * 100 : 100;

            setItem(slots[i], new ItemBuilder(material)
                    .name("&6" + formatCropName(crop))
                    .lore(
                            "&7Total Harvested: &e" + NumberUtils.format(harvested),
                            "&7Milestone Level: &a" + milestoneLevel,
                            "",
                            "&7Progress to next milestone:",
                            createProgressBar(progress) + " &e" + String.format("%.1f", Math.min(progress, 100)) + "%",
                            "&7(" + NumberUtils.format(harvested) + "/" + NumberUtils.format(nextMilestone) + ")",
                            "",
                            "&7Rewards at next milestone:",
                            "&a+5 Farming Fortune",
                            "&6+100 Copper"
                    )
                    .amount(Math.max(1, Math.min(milestoneLevel, 64)))
                    .build());
        }

        // Total farming stats
        long totalHarvested = 0;
        for (CropType crop : CropType.values()) {
            Garden.CropMilestone milestone = garden.getMilestone(crop);
            if (milestone != null) {
                totalHarvested += milestone.getTotalHarvested();
            }
        }

        setItem(40, new ItemBuilder(Material.BOOK)
                .name("&e&lTotal Stats")
                .lore(
                        "&7Total Crops Harvested: &a" + NumberUtils.format(totalHarvested),
                        "",
                        "&7Keep farming to unlock more",
                        "&7milestones and rewards!"
                )
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

    private long getNextMilestoneRequirement(int currentLevel) {
        // Milestone requirements increase exponentially
        return switch (currentLevel) {
            case 0 -> 100;
            case 1 -> 500;
            case 2 -> 1000;
            case 3 -> 2500;
            case 4 -> 5000;
            case 5 -> 10000;
            case 6 -> 25000;
            case 7 -> 50000;
            case 8 -> 100000;
            case 9 -> 250000;
            default -> 500000L + (currentLevel - 10) * 100000L;
        };
    }

    private String createProgressBar(double percentage) {
        int filled = (int) (percentage / 10);
        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append("&a■");
            } else {
                bar.append("&7■");
            }
        }
        bar.append("&8]");
        return bar.toString();
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
