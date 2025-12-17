package com.skyblock.skills.listeners;

import com.skyblock.SkyblockPlugin;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.skills.SkillType;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for fishing skill XP.
 */
public class FishingListener implements Listener {

    private final SkyblockPlugin plugin;

    public FishingListener(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item)) return;

        Player player = event.getPlayer();
        Item caughtItem = (Item) event.getCaught();
        ItemStack item = caughtItem.getItemStack();
        Material type = item.getType();

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(player);
        if (sbPlayer == null) return;

        // Get XP for caught item
        double xp = plugin.getSkillManager().getXpSource(SkillType.FISHING, type.name());
        if (xp <= 0) {
            xp = getFishXp(type);
        }

        if (xp > 0) {
            // Add fishing XP
            sbPlayer.addSkillXp("fishing", xp);
        }

        // Add to collection
        String collectionType = getCollectionType(type);
        if (collectionType != null) {
            sbPlayer.addCollection(collectionType, item.getAmount());
        }
    }

    /**
     * Get default XP for a fish type.
     */
    private double getFishXp(Material material) {
        switch (material) {
            case COD:
                return 20;
            case SALMON:
                return 25;
            case TROPICAL_FISH:
                return 30;
            case PUFFERFISH:
                return 35;
            case INK_SAC:
                return 10;
            case LILY_PAD:
                return 5;
            case NAUTILUS_SHELL:
                return 50;
            default:
                return 10;
        }
    }

    /**
     * Get the collection type for a caught item.
     */
    private String getCollectionType(Material material) {
        switch (material) {
            case COD:
                return "cod";
            case SALMON:
                return "salmon";
            case TROPICAL_FISH:
                return "tropical_fish";
            case PUFFERFISH:
                return "pufferfish";
            case INK_SAC:
                return "ink_sac";
            default:
                return null;
        }
    }
}
