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
 * Listener for mining skill XP.
 */
public class MiningListener implements Listener {

    private final SkyblockPlugin plugin;

    public MiningListener(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        Material type = block.getType();

        // Get XP for this block
        double xp = plugin.getSkillManager().getXpSource(SkillType.MINING, type.name());
        if (xp <= 0) return;

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return;

        // Add mining XP
        sbPlayer.addSkillXp("mining", xp);

        // Also add to collection
        String collectionType = getCollectionType(type);
        if (collectionType != null) {
            sbPlayer.addCollection(collectionType, 1);
        }
    }

    /**
     * Get the collection type for a mined block.
     */
    private String getCollectionType(Material material) {
        switch (material) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                return "coal";
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return "iron";
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case NETHER_GOLD_ORE:
                return "gold";
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return "diamond";
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return "emerald";
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return "lapis";
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return "redstone";
            case COBBLESTONE:
            case STONE:
                return "cobblestone";
            case OBSIDIAN:
                return "obsidian";
            default:
                return null;
        }
    }
}
