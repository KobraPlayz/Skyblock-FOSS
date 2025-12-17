package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.player.PlayerProfile;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Profile management menu.
 */
public class ProfileMenu extends AbstractGUI {

    public ProfileMenu(SkyblockPlugin plugin) {
        super(plugin, "&8Profile Management", 4);
    }

    @Override
    protected void build(Player player) {
        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        fillBorder(createFiller());

        if (sbPlayer == null) {
            player.closeInventory();
            return;
        }

        PlayerProfile activeProfile = sbPlayer.getActiveProfile();

        // Load profiles async then update GUI
        plugin.getPlayerManager().getProfiles(player.getUniqueId()).thenAccept(profiles -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                int slot = 10;
                for (PlayerProfile profile : profiles) {
                    if (slot >= 17) break;

                    boolean isActive = activeProfile != null && activeProfile.getId() == profile.getId();

                    setItem(slot, new ItemBuilder(isActive ? Material.LIME_WOOL : Material.WHITE_WOOL)
                            .name("&a&l" + profile.getName())
                            .lore(
                                    "",
                                    "&7Purse: &6" + NumberUtils.formatCoins(profile.getPurse()),
                                    "&7Created: &7" + formatDate(profile.getCreatedAt()),
                                    "",
                                    isActive ? "&a&lCurrently Selected" : "&eClick to switch!"
                            )
                            .build(), event -> {
                        if (!isActive) {
                            plugin.getPlayerManager().switchProfile(player, profile.getId())
                                    .thenAccept(success -> {
                                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                                            if (success) {
                                                playSuccessSound(player);
                                                String msg = plugin.getConfigManager().getMessage("profile.switched")
                                                        .replace("{name}", profile.getName());
                                                player.sendMessage(ColorUtils.colorize(msg));
                                                player.closeInventory();
                                            } else {
                                                playErrorSound(player);
                                                player.sendMessage(ColorUtils.colorize(
                                                        plugin.getConfigManager().getMessage("general.cooldown")
                                                                .replace("{time}", "5s")));
                                            }
                                        });
                                    });
                        }
                    });

                    slot++;
                }

                // Create new profile button
                int maxProfiles = plugin.getConfigManager().getConfig().getInt("profiles.max-profiles", 5);
                if (profiles.size() < maxProfiles) {
                    setItem(22, new ItemBuilder(Material.EMERALD_BLOCK)
                            .name("&a&lCreate New Profile")
                            .lore(
                                    "&7Create a new SkyBlock",
                                    "&7profile to start fresh!",
                                    "",
                                    "&7Profiles: &e" + profiles.size() + "&7/&a" + maxProfiles,
                                    "",
                                    "&eClick to create!"
                            )
                            .build(), event -> {
                        // Create new profile with next available name
                        List<String> names = plugin.getConfigManager().getConfig()
                                .getStringList("profiles.names");
                        String newName = null;
                        for (String name : names) {
                            boolean taken = false;
                            for (PlayerProfile p : profiles) {
                                if (p.getName().equalsIgnoreCase(name)) {
                                    taken = true;
                                    break;
                                }
                            }
                            if (!taken) {
                                newName = name;
                                break;
                            }
                        }

                        if (newName != null) {
                            String finalName = newName;
                            plugin.getPlayerManager().createNewProfile(player.getUniqueId(), finalName)
                                    .thenAccept(profile -> {
                                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                                            if (profile != null) {
                                                playSuccessSound(player);
                                                String msg = plugin.getConfigManager().getMessage("profile.created")
                                                        .replace("{name}", finalName);
                                                player.sendMessage(ColorUtils.colorize(msg));
                                                build(player); // Refresh
                                            }
                                        });
                                    });
                        }
                    });
                }
            });
        });

        // Back button
        setItem(31, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new SkyblockMenu(plugin));
        });
    }

    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(new java.util.Date(timestamp));
    }
}
