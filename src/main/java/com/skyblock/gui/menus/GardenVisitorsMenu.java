package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.garden.Garden;
import com.skyblock.garden.GardenVisitor;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Garden Visitors menu.
 */
public class GardenVisitorsMenu extends AbstractGUI {

    private final UUID playerUuid;

    public GardenVisitorsMenu(SkyblockPlugin plugin, UUID playerUuid) {
        super(plugin, "&b&lGarden Visitors", 4);
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
        setItem(4, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
                .name("&b&lGarden Visitors")
                .lore(
                        "&7Visitors come to your garden",
                        "&7requesting items!",
                        "",
                        "&7Complete their requests to",
                        "&7earn &6Copper &7and other rewards.",
                        "",
                        "&7Active Visitors: &e" + garden.getActiveVisitors().size() + "/5"
                )
                .build());

        // Display active visitors
        List<GardenVisitor> visitors = garden.getActiveVisitors();
        int[] visitorSlots = {19, 21, 22, 23, 25};

        for (int i = 0; i < visitorSlots.length; i++) {
            if (i < visitors.size()) {
                GardenVisitor visitor = visitors.get(i);
                setItem(visitorSlots[i], createVisitorItem(visitor, player), event -> {
                    // Try to complete the visitor's request
                    if (hasRequiredItems(player, visitor)) {
                        completeVisitorRequest(player, garden, visitor);
                    } else {
                        player.sendMessage("§cYou don't have the required items!");
                        playErrorSound(player);
                    }
                });
            } else {
                // Empty visitor slot
                setItem(visitorSlots[i], new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .name("&8Empty Slot")
                        .lore(
                                "&7A visitor may arrive soon...",
                                "",
                                "&7Visitors arrive every 15 minutes."
                        )
                        .build());
            }
        }

        // Spawn new visitor button (for testing)
        if (visitors.size() < 5) {
            setItem(31, new ItemBuilder(Material.EMERALD)
                    .name("&aSpawn Visitor")
                    .lore(
                            "&7Spawn a new visitor!",
                            "&8(Debug feature)",
                            "",
                            "&eClick to spawn!"
                    )
                    .build(), event -> {
                plugin.getGardenManager().spawnVisitor(garden);
                plugin.getGuiManager().openGUI(player, new GardenVisitorsMenu(plugin, playerUuid));
            });
        }

        // Back button
        setItem(27, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenMenu(plugin, playerUuid));
        });

        // Close button
        setItem(35, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private ItemStack createVisitorItem(GardenVisitor visitor, Player player) {
        Material requestMaterial = getMaterialFromRequest(visitor.getRequestedItem());
        boolean hasItems = hasRequiredItems(player, visitor);

        List<String> lore = new ArrayList<>();
        lore.add("&7" + visitor.getName() + " wants:");
        lore.add("");
        lore.add("&e" + visitor.getRequestedAmount() + "x " + formatItemName(visitor.getRequestedItem()));
        lore.add("");
        lore.add("&7Reward:");
        lore.add("&6+" + NumberUtils.format(visitor.getCopperReward()) + " Copper");
        if (visitor.getGardenXpReward() > 0) {
            lore.add("&a+" + visitor.getGardenXpReward() + " Garden XP");
        }
        lore.add("");
        lore.add("&7Time remaining: &e" + formatTime(visitor.getExpirationTime() - System.currentTimeMillis()));
        lore.add("");
        lore.add(hasItems ? "&aClick to complete!" : "&cYou need more items!");

        return new ItemBuilder(Material.PLAYER_HEAD)
                .name("&b" + visitor.getName())
                .lore(lore.toArray(new String[0]))
                .build();
    }

    private boolean hasRequiredItems(Player player, GardenVisitor visitor) {
        Material material = getMaterialFromRequest(visitor.getRequestedItem());
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= visitor.getRequestedAmount();
    }

    private void completeVisitorRequest(Player player, Garden garden, GardenVisitor visitor) {
        Material material = getMaterialFromRequest(visitor.getRequestedItem());
        int remaining = visitor.getRequestedAmount();

        // Remove items from inventory
        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) {
                int amount = item.getAmount();
                if (amount <= remaining) {
                    player.getInventory().setItem(i, null);
                    remaining -= amount;
                } else {
                    item.setAmount(amount - remaining);
                    remaining = 0;
                }
            }
        }

        // Give rewards
        garden.setCopper(garden.getCopper() + visitor.getCopperReward());
        garden.setXp(garden.getXp() + visitor.getGardenXpReward());
        garden.getActiveVisitors().remove(visitor);
        plugin.getGardenManager().saveGarden(garden);

        player.sendMessage("§aCompleted " + visitor.getName() + "'s request!");
        player.sendMessage("§6+" + NumberUtils.format(visitor.getCopperReward()) + " Copper");
        if (visitor.getGardenXpReward() > 0) {
            player.sendMessage("§a+" + visitor.getGardenXpReward() + " Garden XP");
        }
        playSuccessSound(player);
        plugin.getGuiManager().openGUI(player, new GardenVisitorsMenu(plugin, playerUuid));
    }

    private Material getMaterialFromRequest(String itemName) {
        return switch (itemName.toUpperCase()) {
            case "WHEAT" -> Material.WHEAT;
            case "CARROT", "CARROTS" -> Material.CARROT;
            case "POTATO", "POTATOES" -> Material.POTATO;
            case "PUMPKIN" -> Material.PUMPKIN;
            case "MELON" -> Material.MELON_SLICE;
            case "COCOA_BEANS", "COCOA" -> Material.COCOA_BEANS;
            case "CACTUS" -> Material.CACTUS;
            case "SUGAR_CANE" -> Material.SUGAR_CANE;
            case "NETHER_WART" -> Material.NETHER_WART;
            case "MUSHROOM", "RED_MUSHROOM" -> Material.RED_MUSHROOM;
            default -> Material.WHEAT;
        };
    }

    private String formatItemName(String itemName) {
        return itemName.replace("_", " ");
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "Expired";
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        return minutes + "m " + seconds + "s";
    }
}
