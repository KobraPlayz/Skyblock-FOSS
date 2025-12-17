package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.island.Island;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Island Settings GUI menu.
 */
public class IslandSettingsMenu extends AbstractGUI {

    private final UUID ownerUuid;

    public IslandSettingsMenu(SkyblockPlugin plugin, UUID ownerUuid) {
        super(plugin, "&8Island Settings", 5);
        this.ownerUuid = ownerUuid;
    }

    @Override
    protected void build(Player player) {
        Island island = plugin.getIslandManager().getIsland(ownerUuid);
        if (island == null) {
            player.closeInventory();
            player.sendMessage("§cYou don't have an island!");
            return;
        }

        fillBorder(createFiller());

        // Island Info
        setItem(4, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&a&lYour Island")
                .lore(
                        "&7Size: &e" + island.getSizeX() + "x" + island.getSizeZ(),
                        "&7Members: &e" + island.getMembers().size(),
                        "&7Total Visitors: &e" + island.getTotalVisitors(),
                        "",
                        "&7Spawn: &e" + formatLocation(island)
                )
                .build());

        // Public/Private Toggle
        boolean isPublic = island.getSetting("public", false);
        setItem(20, new ItemBuilder(isPublic ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(isPublic ? "&a&lPublic Island" : "&c&lPrivate Island")
                .lore(
                        "&7Status: " + (isPublic ? "&aPublic" : "&cPrivate"),
                        "",
                        isPublic ? "&7Anyone can visit your island." : "&7Only invited players can visit.",
                        "",
                        "&eClick to toggle!"
                )
                .build(), event -> {
            island.setSetting("public", !isPublic);
            plugin.getIslandManager().saveIsland(island);
            plugin.getGuiManager().openGUI(player, new IslandSettingsMenu(plugin, ownerUuid));
        });

        // PvP Toggle
        boolean pvpEnabled = island.getSetting("pvp", false);
        setItem(22, new ItemBuilder(pvpEnabled ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD)
                .name(pvpEnabled ? "&c&lPvP Enabled" : "&a&lPvP Disabled")
                .lore(
                        "&7Status: " + (pvpEnabled ? "&cEnabled" : "&aDisabled"),
                        "",
                        pvpEnabled ? "&7Players can fight on your island." : "&7Players cannot fight on your island.",
                        "",
                        "&eClick to toggle!"
                )
                .hideFlags()
                .build(), event -> {
            island.setSetting("pvp", !pvpEnabled);
            plugin.getIslandManager().saveIsland(island);
            plugin.getGuiManager().openGUI(player, new IslandSettingsMenu(plugin, ownerUuid));
        });

        // Mob Spawning Toggle
        boolean mobSpawning = island.getSetting("mob_spawning", true);
        setItem(24, new ItemBuilder(mobSpawning ? Material.ZOMBIE_HEAD : Material.BARRIER)
                .name(mobSpawning ? "&a&lMob Spawning Enabled" : "&c&lMob Spawning Disabled")
                .lore(
                        "&7Status: " + (mobSpawning ? "&aEnabled" : "&cDisabled"),
                        "",
                        mobSpawning ? "&7Mobs can spawn on your island." : "&7Mobs cannot spawn on your island.",
                        "",
                        "&eClick to toggle!"
                )
                .build(), event -> {
            island.setSetting("mob_spawning", !mobSpawning);
            plugin.getIslandManager().saveIsland(island);
            plugin.getGuiManager().openGUI(player, new IslandSettingsMenu(plugin, ownerUuid));
        });

        // Visitor Permissions
        setItem(30, new ItemBuilder(Material.CHEST)
                .name("&e&lVisitor Permissions")
                .lore(
                        "&7Configure what visitors can do",
                        "&7on your island.",
                        "",
                        "&7Container Access: " + (island.getSetting("visitor_containers", false) ? "&aAllowed" : "&cDenied"),
                        "",
                        "&eClick to configure!"
                )
                .build(), event -> {
            // Toggle visitor container access
            boolean containers = island.getSetting("visitor_containers", false);
            island.setSetting("visitor_containers", !containers);
            plugin.getIslandManager().saveIsland(island);
            plugin.getGuiManager().openGUI(player, new IslandSettingsMenu(plugin, ownerUuid));
        });

        // Animal Spawning Toggle
        boolean animalSpawning = island.getSetting("animal_spawning", true);
        setItem(32, new ItemBuilder(animalSpawning ? Material.PIG_SPAWN_EGG : Material.BARRIER)
                .name(animalSpawning ? "&a&lAnimal Spawning Enabled" : "&c&lAnimal Spawning Disabled")
                .lore(
                        "&7Status: " + (animalSpawning ? "&aEnabled" : "&cDisabled"),
                        "",
                        animalSpawning ? "&7Animals can spawn on your island." : "&7Animals cannot spawn on your island.",
                        "",
                        "&eClick to toggle!"
                )
                .build(), event -> {
            island.setSetting("animal_spawning", !animalSpawning);
            plugin.getIslandManager().saveIsland(island);
            plugin.getGuiManager().openGUI(player, new IslandSettingsMenu(plugin, ownerUuid));
        });

        // Set Island Spawn
        setItem(38, new ItemBuilder(Material.RED_BED)
                .name("&b&lSet Island Spawn")
                .lore(
                        "&7Set your island's spawn point",
                        "&7to your current location.",
                        "",
                        "&eClick to set!"
                )
                .build(), event -> {
            player.closeInventory();
            plugin.getIslandManager().setIslandSpawn(ownerUuid, player.getLocation());
            player.sendMessage("§aIsland spawn point set to your current location!");
        });

        // Reset Island
        setItem(40, new ItemBuilder(Material.TNT)
                .name("&c&lReset Island")
                .lore(
                        "&7&lWARNING: This will delete",
                        "&7&lyour entire island!",
                        "",
                        "&7All builds, items, and progress",
                        "&7will be permanently lost.",
                        "",
                        "&cShift+Click to reset!"
                )
                .build(), event -> {
            if (event.isShiftClick()) {
                player.closeInventory();
                plugin.getIslandManager().resetIsland(ownerUuid);
                player.sendMessage("§cYour island has been reset!");
            } else {
                player.sendMessage("§cShift+Click to confirm island reset!");
            }
        });

        // View Banned Players
        setItem(42, new ItemBuilder(Material.PAPER)
                .name("&c&lBanned Players")
                .lore(
                        "&7View players banned from",
                        "&7your island.",
                        "",
                        "&7Banned: &e" + island.getBannedPlayers().size() + " players",
                        "",
                        "&eClick to view!"
                )
                .build(), event -> {
            player.closeInventory();
            player.sendMessage("§eBanned players: " + island.getBannedPlayers().size());
            // Could open a banned players menu here
        });

        // Back button
        setItem(36, createBackButton(), event -> {
            player.closeInventory();
        });

        // Close button
        setItem(44, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    private String formatLocation(Island island) {
        if (island.getSpawnX() == 0 && island.getSpawnY() == 0 && island.getSpawnZ() == 0) {
            return "Not set";
        }
        return String.format("%.0f, %.0f, %.0f", island.getSpawnX(), island.getSpawnY(), island.getSpawnZ());
    }
}
