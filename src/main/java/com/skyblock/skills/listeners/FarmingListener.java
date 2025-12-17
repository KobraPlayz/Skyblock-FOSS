package com.skyblock.skills.listeners;

import com.skyblock.SkyblockPlugin;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.skills.SkillType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listener for farming skill XP.
 */
public class FarmingListener implements Listener {

    private final SkyblockPlugin plugin;

    public FarmingListener(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        Material type = block.getType();

        // Check if it's a crop
        if (!isCrop(type)) return;

        // Check if crop is fully grown
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() < ageable.getMaximumAge()) {
                return; // Not fully grown
            }
        }

        // Get the drop material for XP calculation
        String xpSource = getCropXpSource(type);
        if (xpSource == null) return;

        double xp = plugin.getSkillManager().getXpSource(SkillType.FARMING, xpSource);
        if (xp <= 0) return;

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return;

        // Add farming XP
        sbPlayer.addSkillXp("farming", xp);

        // Add to collection
        String collectionType = getCollectionType(type);
        if (collectionType != null) {
            sbPlayer.addCollection(collectionType, 1);
        }
    }

    /**
     * Check if a material is a farmable crop.
     */
    private boolean isCrop(Material material) {
        switch (material) {
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case MELON:
            case PUMPKIN:
            case SUGAR_CANE:
            case COCOA:
            case CACTUS:
            case NETHER_WART:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case MUSHROOM_STEM:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the XP source key for a crop.
     */
    private String getCropXpSource(Material material) {
        switch (material) {
            case WHEAT:
                return "WHEAT";
            case CARROTS:
                return "CARROTS";
            case POTATOES:
                return "POTATOES";
            case BEETROOTS:
                return "BEETROOTS";
            case MELON:
                return "MELON_SLICE";
            case PUMPKIN:
                return "PUMPKIN";
            case SUGAR_CANE:
                return "SUGAR_CANE";
            case COCOA:
                return "COCOA_BEANS";
            case CACTUS:
                return "CACTUS";
            case NETHER_WART:
                return "NETHER_WART";
            case RED_MUSHROOM:
                return "RED_MUSHROOM";
            case BROWN_MUSHROOM:
                return "BROWN_MUSHROOM";
            case MUSHROOM_STEM:
                return "MUSHROOM_STEM";
            default:
                return null;
        }
    }

    /**
     * Get the collection type for a crop.
     */
    private String getCollectionType(Material material) {
        switch (material) {
            case WHEAT:
                return "wheat";
            case CARROTS:
                return "carrot";
            case POTATOES:
                return "potato";
            case MELON:
                return "melon";
            case PUMPKIN:
                return "pumpkin";
            case SUGAR_CANE:
                return "sugar_cane";
            case NETHER_WART:
                return "nether_wart";
            default:
                return null;
        }
    }
}
