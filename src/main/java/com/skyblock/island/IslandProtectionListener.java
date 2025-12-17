package com.skyblock.island;

import com.skyblock.SkyblockPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles island protection and permissions.
 */
public class IslandProtectionListener implements Listener {

    private final SkyblockPlugin plugin;
    private final IslandManager islandManager;

    public IslandProtectionListener(SkyblockPlugin plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
    }

    /**
     * Check if a location is on an island world.
     */
    private Island getIslandAtLocation(Location location) {
        if (location == null || location.getWorld() == null) return null;

        String worldName = location.getWorld().getName();
        if (!worldName.startsWith("island_")) return null;

        return islandManager.getIslandByWorld(worldName);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Island island = getIslandAtLocation(block.getLocation());
        if (island == null) return;

        if (!island.canBuild(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to break blocks here!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Island island = getIslandAtLocation(block.getLocation());
        if (island == null) return;

        if (!island.canBuild(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to place blocks here!");
        }

        // Check island boundaries
        if (!island.isWithinBounds(block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cYou can't build outside your island boundaries!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        Island island = getIslandAtLocation(block.getLocation());
        if (island == null) return;

        // Allow certain interactions for visitors
        Material type = block.getType();
        boolean isInteractable = isInteractableBlock(type);

        if (!island.canInteract(player.getUniqueId()) && !isInteractable) {
            event.setCancelled(true);
        }

        // Block container access for visitors unless allowed
        if (isContainer(type) && !island.isMember(player.getUniqueId())) {
            if (!island.getBooleanSetting("allow_visitor_container_access", false)) {
                event.setCancelled(true);
                player.sendMessage("§cYou don't have permission to access containers here!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        Island island = getIslandAtLocation(entity.getLocation());
        if (island == null) return;

        // Block interacting with armor stands, item frames for visitors
        if (!island.isMember(player.getUniqueId())) {
            String entityType = entity.getType().name();
            if (entityType.equals("ARMOR_STAND") || entityType.equals("ITEM_FRAME") ||
                entityType.equals("GLOW_ITEM_FRAME")) {
                if (!island.getBooleanSetting("allow_visitor_entity_interact", false)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Island island = getIslandAtLocation(block.getLocation());
        if (island == null) return;

        if (!island.canBuild(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to use buckets here!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Island island = getIslandAtLocation(block.getLocation());
        if (island == null) return;

        if (!island.canBuild(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to use buckets here!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Island island = getIslandAtLocation(event.getEntity().getLocation());
        if (island == null) return;

        if (!island.canBuild(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to break this!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle PvP
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof Player victim) {
            Island island = getIslandAtLocation(victim.getLocation());
            if (island != null && !island.isPvpEnabled()) {
                event.setCancelled(true);
                attacker.sendMessage("§cPvP is disabled on this island!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();

        Island island = getIslandAtLocation(location);
        if (island == null) return;

        // Control mob spawning based on settings
        if (entity instanceof Monster) {
            if (!island.getBooleanSetting("mob_spawning", true)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Check if a block is interactable (doors, buttons, etc.)
     */
    private boolean isInteractableBlock(Material type) {
        String name = type.name();
        return name.contains("DOOR") ||
               name.contains("GATE") ||
               name.contains("BUTTON") ||
               name.contains("LEVER") ||
               name.contains("PRESSURE_PLATE") ||
               name.equals("BELL") ||
               name.contains("TRAPDOOR");
    }

    /**
     * Check if a block is a container.
     */
    private boolean isContainer(Material type) {
        String name = type.name();
        return name.contains("CHEST") ||
               name.contains("BARREL") ||
               name.contains("SHULKER") ||
               name.equals("HOPPER") ||
               name.equals("DROPPER") ||
               name.equals("DISPENSER") ||
               name.equals("FURNACE") ||
               name.equals("BLAST_FURNACE") ||
               name.equals("SMOKER") ||
               name.equals("BREWING_STAND");
    }
}
