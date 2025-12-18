package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Main Skyblock menu GUI.
 */
public class SkyblockMenu extends AbstractGUI {

    public SkyblockMenu(SkyblockPlugin plugin) {
        super(plugin, "&8SkyBlock Menu", 6);
    }

    @Override
    protected void build(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        fillBorder(createFiller());

        // Your Skills
        setItem(20, new ItemBuilder(Material.DIAMOND_SWORD)
                .name("&a&lYour Skills")
                .lore(
                        "&7View all of your Skills and",
                        "&7their rewards!",
                        "",
                        "&7Skill Average: &6" + (sbPlayer != null ? String.format("%.1f", sbPlayer.getSkillAverage()) : "0"),
                        "",
                        "&eClick to view!"
                )
                .hideFlags()
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new SkillsMenu(plugin));
        });

        // Collections
        setItem(22, new ItemBuilder(Material.PAINTING)
                .name("&e&lCollections")
                .lore(
                        "&7View all of the items available",
                        "&7in SkyBlock. Collect more to unlock",
                        "&7awesome rewards!",
                        "",
                        "&eClick to view!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new CollectionsMenu(plugin));
        });

        // Profile Management
        setItem(24, new ItemBuilder(Material.NAME_TAG)
                .name("&b&lProfile Management")
                .lore(
                        "&7Manage your SkyBlock profiles.",
                        "",
                        "&7Current: &e" + (sbPlayer != null && sbPlayer.getActiveProfile() != null ?
                                sbPlayer.getActiveProfile().getName() : "None"),
                        "",
                        "&eClick to manage!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new ProfileMenu(plugin));
        });

        // Phase 1.5 - Island button
        setItem(28, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&a&lYour Island")
                .lore(
                        "&7Manage your private island!",
                        "",
                        "&7Build, farm, and invite friends",
                        "&7to your personal SkyBlock island.",
                        "",
                        "&eClick to teleport!",
                        "&eShift+Click for settings!"
                )
                .build(), event -> {
            if (event.isShiftClick()) {
                plugin.getGuiManager().openGUI(player, new IslandSettingsMenu(plugin, player.getUniqueId()));
            } else {
                player.closeInventory();
                plugin.getIslandManager().teleportToIsland(player, player.getUniqueId());
            }
        });

        // Economy/Coins info
        setItem(30, new ItemBuilder(Material.GOLD_INGOT)
                .name("&6&lPurse")
                .lore(
                        "&7Your current coins.",
                        "",
                        "&7Coins: &6" + (sbPlayer != null ?
                                NumberUtils.formatCoins(sbPlayer.getPurse()) : "0"),
                        "",
                        "&eClick for more info!"
                )
                .build(), event -> {
            // Show more economy info
        });

        // Phase 1.5 - Garden button
        setItem(34, new ItemBuilder(Material.WHEAT)
                .name("&2&lThe Garden")
                .lore(
                        "&7Your personal farming paradise!",
                        "",
                        "&7Grow crops, earn Copper, and",
                        "&7unlock crop upgrades.",
                        "",
                        "&7Requires: &aSkyBlock Level 5",
                        "",
                        "&eClick to open!"
                )
                .build(), event -> {
            plugin.getGuiManager().openGUI(player, new GardenMenu(plugin, player.getUniqueId()));
        });

        // Phase 2 - Pets
        if (plugin.getModuleManager().isModuleEnabled("pets")) {
            setItem(32, new ItemBuilder(Material.BONE)
                    .name("&6&lPets")
                    .lore(
                            "&7View your pets and level them up",
                            "&7to gain powerful bonuses!",
                            "",
                            "&7Pets gain XP when you train",
                            "&7skills that match their type.",
                            "",
                            "&eClick to view!"
                    )
                    .build(), event -> {
                plugin.getGuiManager().openGUI(player, new PetMenu(plugin));
            });
        } else if (plugin.getModuleManager().isComingSoon("pets")) {
            setItem(32, new ItemBuilder(Material.BONE)
                    .name("&8Pets")
                    .lore(
                            "&8Coming in Phase 2!",
                            "",
                            "&7Collect and level up pets",
                            "&7to gain special bonuses!"
                    )
                    .build());
        }

        if (plugin.getModuleManager().isComingSoon("accessories")) {
            setItem(38, new ItemBuilder(Material.PLAYER_HEAD)
                    .name("&8Accessory Bag")
                    .lore(
                            "&8Coming in Phase 2!",
                            "",
                            "&7Store accessories for",
                            "&7passive stat bonuses!"
                    )
                    .build());
        }

        if (plugin.getModuleManager().isComingSoon("bazaar")) {
            setItem(40, new ItemBuilder(Material.GOLD_BLOCK)
                    .name("&8Bazaar")
                    .lore(
                            "&8Coming in Phase 3!",
                            "",
                            "&7Buy and sell items on",
                            "&7the marketplace!"
                    )
                    .build());
        }

        if (plugin.getModuleManager().isComingSoon("auction-house")) {
            setItem(42, new ItemBuilder(Material.GOLDEN_HORSE_ARMOR)
                    .name("&8Auction House")
                    .lore(
                            "&8Coming in Phase 3!",
                            "",
                            "&7Buy and sell unique items",
                            "&7through auctions!"
                    )
                    .build());
        }

        // Admin panel (if admin)
        if (player.hasPermission("skyblock.admin")) {
            setItem(49, new ItemBuilder(Material.COMMAND_BLOCK)
                    .name("&c&lAdmin Panel")
                    .lore(
                            "&7Access administrator tools.",
                            "",
                            "&eClick to open!"
                    )
                    .build(), event -> {
                plugin.getGuiManager().openGUI(player, new AdminMenu(plugin));
            });
        }

        // Close button
        setItem(53, createCloseButton(), event -> {
            player.closeInventory();
        });
    }
}
