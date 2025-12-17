package com.skyblock.skills.listeners;

import com.skyblock.SkyblockPlugin;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.skills.SkillType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listener for foraging skill XP.
 */
public class ForagingListener implements Listener {

    private final SkyblockPlugin plugin;

    public ForagingListener(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        Material type = block.getType();

        // Check if it's a log
        if (!isLog(type)) return;

        // Get XP for this log type
        double xp = plugin.getSkillManager().getXpSource(SkillType.FORAGING, type.name());
        if (xp <= 0) {
            xp = 6; // Default log XP
        }

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return;

        // Add foraging XP
        sbPlayer.addSkillXp("foraging", xp);

        // Add to collection
        String collectionType = getCollectionType(type);
        if (collectionType != null) {
            sbPlayer.addCollection(collectionType, 1);
        }
    }

    /**
     * Check if a material is a log.
     */
    private boolean isLog(Material material) {
        switch (material) {
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
            case CHERRY_LOG:
            case CRIMSON_STEM:
            case WARPED_STEM:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the collection type for a log.
     */
    private String getCollectionType(Material material) {
        switch (material) {
            case OAK_LOG:
                return "oak_log";
            case SPRUCE_LOG:
                return "spruce_log";
            case BIRCH_LOG:
                return "birch_log";
            case JUNGLE_LOG:
                return "jungle_log";
            case ACACIA_LOG:
                return "acacia_log";
            case DARK_OAK_LOG:
                return "dark_oak_log";
            default:
                return null;
        }
    }
}
