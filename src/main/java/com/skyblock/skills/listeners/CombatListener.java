package com.skyblock.skills.listeners;

import com.skyblock.SkyblockPlugin;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.skills.SkillType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Listener for combat skill XP.
 */
public class CombatListener implements Listener {

    private final SkyblockPlugin plugin;

    public CombatListener(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;
        if (entity.getType() == EntityType.PLAYER) return; // Don't give XP for killing players

        SkyblockPlayer sbPlayer = plugin.getPlayerManager().getPlayer(killer);
        if (sbPlayer == null) return;

        // Get XP for this mob
        double xp = plugin.getSkillManager().getXpSource(SkillType.COMBAT, entity.getType().name());
        if (xp <= 0) {
            // Default XP based on max health
            xp = Math.max(1, entity.getMaxHealth() / 2);
        }

        // Add combat XP
        sbPlayer.addSkillXp("combat", xp);

        // Add to collection for drops
        String collectionType = getCollectionType(entity.getType());
        if (collectionType != null) {
            sbPlayer.addCollection(collectionType, 1);
        }
    }

    /**
     * Get the collection type for a mob's drops.
     */
    private String getCollectionType(EntityType entityType) {
        switch (entityType) {
            case ZOMBIE:
            case ZOMBIE_VILLAGER:
            case HUSK:
            case DROWNED:
            case ZOMBIFIED_PIGLIN:
                return "rotten_flesh";
            case SKELETON:
            case STRAY:
            case WITHER_SKELETON:
                return "bone";
            case SPIDER:
            case CAVE_SPIDER:
                return "string";
            case ENDERMAN:
                return "ender_pearl";
            case BLAZE:
                return "blaze_rod";
            case SLIME:
                return "slime_ball";
            case MAGMA_CUBE:
                return "magma_cream";
            case GHAST:
                return "ghast_tear";
            default:
                return null;
        }
    }
}
