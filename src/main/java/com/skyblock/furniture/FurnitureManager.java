package com.skyblock.furniture;

import com.skyblock.SkyblockPlugin;
import com.skyblock.island.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages furniture placement and interaction.
 */
public class FurnitureManager {

    private final SkyblockPlugin plugin;

    // Furniture by island ID
    private final Map<UUID, List<Furniture>> islandFurniture;

    // Spawned entities for furniture
    private final Map<UUID, Entity> furnitureEntities;

    // Configuration
    private final int maxPerIsland;
    private final boolean allowFunctional;

    public FurnitureManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.islandFurniture = new ConcurrentHashMap<>();
        this.furnitureEntities = new ConcurrentHashMap<>();

        // Load config
        maxPerIsland = plugin.getConfigManager().getFurnitureConfig()
            .getInt("furniture.max_per_island", 15);
        allowFunctional = plugin.getConfigManager().getFurnitureConfig()
            .getBoolean("furniture.allow_functional", true);
    }

    /**
     * Place furniture at a location.
     */
    public boolean placeFurniture(Player player, Island island, FurnitureType type, Location location) {
        if (!plugin.getModuleManager().isModuleEnabled("furniture")) {
            player.sendMessage("§cFurniture is currently disabled!");
            return false;
        }

        // Check limit
        List<Furniture> existing = islandFurniture.getOrDefault(island.getId(), new ArrayList<>());
        if (existing.size() >= maxPerIsland) {
            player.sendMessage("§cYou have reached the furniture limit (" + maxPerIsland + ")!");
            return false;
        }

        // Check if functional is allowed
        if (type.isFunctional() && !allowFunctional) {
            player.sendMessage("§cFunctional furniture is disabled on this server!");
            return false;
        }

        // Check permissions
        if (!island.canBuild(player.getUniqueId())) {
            player.sendMessage("§cYou don't have permission to place furniture here!");
            return false;
        }

        // Create furniture
        Furniture furniture = new Furniture(island.getId(), type, location, player.getUniqueId());

        // Add to list
        existing.add(furniture);
        islandFurniture.put(island.getId(), existing);

        // Save to database
        saveFurniture(furniture);

        // Spawn visual entity
        spawnFurnitureEntity(furniture, location.getWorld());

        player.sendMessage("§aPlaced " + type.getDisplayName() + "!");
        return true;
    }

    /**
     * Remove furniture.
     */
    public boolean removeFurniture(Player player, Island island, Furniture furniture) {
        if (!island.canBuild(player.getUniqueId())) {
            player.sendMessage("§cYou don't have permission to remove furniture here!");
            return false;
        }

        List<Furniture> existing = islandFurniture.get(island.getId());
        if (existing != null) {
            existing.remove(furniture);
        }

        // Remove entity
        Entity entity = furnitureEntities.remove(furniture.getId());
        if (entity != null) {
            entity.remove();
        }

        // Delete from database
        deleteFurniture(furniture.getId());

        // Give item back to player
        ItemStack item = createFurnitureItem(furniture.getType());
        player.getInventory().addItem(item);

        player.sendMessage("§aPickup " + furniture.getType().getDisplayName() + "!");
        return true;
    }

    /**
     * Get furniture at a location.
     */
    public Furniture getFurnitureAt(Island island, Location location) {
        List<Furniture> furniture = islandFurniture.get(island.getId());
        if (furniture == null) return null;

        for (Furniture f : furniture) {
            double distance = Math.sqrt(
                Math.pow(f.getX() - location.getX(), 2) +
                Math.pow(f.getY() - location.getY(), 2) +
                Math.pow(f.getZ() - location.getZ(), 2)
            );
            if (distance < 1.0) {
                return f;
            }
        }
        return null;
    }

    /**
     * Get all furniture for an island.
     */
    public List<Furniture> getIslandFurniture(UUID islandId) {
        return new ArrayList<>(islandFurniture.getOrDefault(islandId, new ArrayList<>()));
    }

    /**
     * Load furniture for an island.
     */
    public CompletableFuture<List<Furniture>> loadFurniture(UUID islandId) {
        return plugin.getDatabaseManager().executeAsync(conn -> {
            List<Furniture> furniture = new ArrayList<>();

            String sql = "SELECT * FROM furniture WHERE island_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, islandId.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        FurnitureType type = FurnitureType.fromString(rs.getString("furniture_type"));
                        if (type == null) continue;

                        Furniture f = new Furniture(
                            id,
                            islandId,
                            type,
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            UUID.fromString(rs.getString("placed_by")),
                            rs.getLong("placed_at")
                        );
                        f.setDataJson(rs.getString("data_json"));
                        furniture.add(f);
                    }
                }
            }

            islandFurniture.put(islandId, furniture);
            return furniture;
        });
    }

    /**
     * Spawn all furniture entities for an island when the world loads.
     */
    public void spawnIslandFurniture(UUID islandId, World world) {
        List<Furniture> furniture = islandFurniture.get(islandId);
        if (furniture == null) return;

        for (Furniture f : furniture) {
            spawnFurnitureEntity(f, world);
        }
    }

    /**
     * Remove all furniture entities for an island.
     */
    public void despawnIslandFurniture(UUID islandId) {
        List<Furniture> furniture = islandFurniture.get(islandId);
        if (furniture == null) return;

        for (Furniture f : furniture) {
            Entity entity = furnitureEntities.remove(f.getId());
            if (entity != null) {
                entity.remove();
            }
        }
    }

    /**
     * Spawn a furniture entity.
     */
    private void spawnFurnitureEntity(Furniture furniture, World world) {
        Location loc = furniture.getLocation(world);

        // Use armor stand as base entity
        ArmorStand stand = world.spawn(loc, ArmorStand.class, entity -> {
            entity.setGravity(false);
            entity.setVisible(false);
            entity.setInvulnerable(true);
            entity.setSmall(true);
            entity.setMarker(true);

            // Set item on head for visual
            ItemStack displayItem = new ItemStack(furniture.getType().getMaterial());
            entity.getEquipment().setHelmet(displayItem);

            // Add metadata for identification
            entity.setCustomName("furniture:" + furniture.getId());
            entity.setCustomNameVisible(false);
        });

        furnitureEntities.put(furniture.getId(), stand);
    }

    /**
     * Create a furniture item for inventory.
     */
    public ItemStack createFurnitureItem(FurnitureType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + type.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Category: " + type.getCategory().getColoredName());
            lore.add("");
            lore.add("§eRight-click to place!");

            meta.setLore(lore);

            // Add NBT tag for identification
            // Using PersistentDataContainer would be better but this is simplified
            meta.setLocalizedName("furniture:" + type.name());

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Check if an item is furniture.
     */
    public FurnitureType getFurnitureFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        var meta = item.getItemMeta();
        if (meta == null) return null;

        String localized = meta.getLocalizedName();
        if (localized != null && localized.startsWith("furniture:")) {
            String typeName = localized.substring(10);
            return FurnitureType.fromString(typeName);
        }

        return null;
    }

    /**
     * Handle furniture interaction.
     */
    public void handleInteraction(Player player, Furniture furniture) {
        FurnitureType type = furniture.getType();

        if (type.isCosmetic()) {
            // Just display info
            player.sendMessage("§6" + type.getDisplayName());
            return;
        }

        // Functional furniture
        switch (type) {
            case CHEST_PLUS -> openChestPlus(player, furniture);
            case ENCHANTING_TABLE_PLUS -> openEnchantingPlus(player, furniture);
            case CRAFTING_TABLE_PLUS -> openCraftingPlus(player, furniture);
            case ANVIL_PLUS -> openAnvilPlus(player, furniture);
            case FURNACE_PLUS -> openFurnacePlus(player, furniture);
            case COMPOSTER -> openComposter(player, furniture);
        }
    }

    private void openChestPlus(Player player, Furniture furniture) {
        // Open extended storage GUI
        player.sendMessage("§eChest+ functionality coming soon!");
    }

    private void openEnchantingPlus(Player player, Furniture furniture) {
        // Open enchanting with max bookshelves
        player.openEnchanting(furniture.getLocation(player.getWorld()), true);
    }

    private void openCraftingPlus(Player player, Furniture furniture) {
        player.openWorkbench(furniture.getLocation(player.getWorld()), true);
    }

    private void openAnvilPlus(Player player, Furniture furniture) {
        // Open anvil GUI - using direct packet would be needed for true anvil
        player.sendMessage("§eAnvil+ functionality coming soon!");
    }

    private void openFurnacePlus(Player player, Furniture furniture) {
        player.sendMessage("§eFurnace+ functionality coming soon!");
    }

    private void openComposter(Player player, Furniture furniture) {
        // Open composter GUI for converting items to compost
        player.sendMessage("§eComposter functionality coming soon!");
    }

    // Database operations
    private void saveFurniture(Furniture furniture) {
        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            String sql = """
                INSERT INTO furniture (id, island_id, furniture_type, x, y, z, yaw, data_json, placed_at, placed_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, furniture.getId().toString());
                stmt.setString(2, furniture.getIslandId().toString());
                stmt.setString(3, furniture.getType().name());
                stmt.setDouble(4, furniture.getX());
                stmt.setDouble(5, furniture.getY());
                stmt.setDouble(6, furniture.getZ());
                stmt.setFloat(7, furniture.getYaw());
                stmt.setString(8, furniture.getDataJson());
                stmt.setLong(9, furniture.getPlacedAt());
                stmt.setString(10, furniture.getPlacedBy().toString());
                stmt.executeUpdate();
            }
        });
    }

    private void deleteFurniture(UUID furnitureId) {
        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            String sql = "DELETE FROM furniture WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, furnitureId.toString());
                stmt.executeUpdate();
            }
        });
    }

    public void shutdown() {
        // Remove all furniture entities
        for (Entity entity : furnitureEntities.values()) {
            entity.remove();
        }
        furnitureEntities.clear();
    }
}
