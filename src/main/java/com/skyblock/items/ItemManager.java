package com.skyblock.items;

import com.skyblock.SkyblockPlugin;
import com.skyblock.items.abilities.AbilityTrigger;
import com.skyblock.items.abilities.ItemAbility;
import com.skyblock.items.rarity.Rarity;
import com.skyblock.items.reforge.Reforge;
import com.skyblock.items.reforge.ReforgeManager;
import com.skyblock.items.stats.ItemStats;
import com.skyblock.items.stats.StatType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages custom items registration, creation, and handling.
 */
public class ItemManager implements Listener {

    private final SkyblockPlugin plugin;
    private final Map<String, CustomItem> itemRegistry;
    private final ReforgeManager reforgeManager;
    private final NamespacedKey itemIdKey;
    private final Map<UUID, Long> cooldowns;

    public ItemManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.itemRegistry = new HashMap<>();
        this.reforgeManager = new ReforgeManager(plugin);
        this.itemIdKey = new NamespacedKey(plugin, CustomItem.NBT_ITEM_ID);
        this.cooldowns = new HashMap<>();

        loadItems();
    }

    /**
     * Load items from configuration.
     */
    private void loadItems() {
        FileConfiguration config = plugin.getConfigManager().getItemsConfig();
        if (config == null) {
            plugin.log(Level.WARNING, "Items config not found!");
            return;
        }

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            plugin.log(Level.WARNING, "No items section found in items.yml!");
            return;
        }

        int count = 0;
        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
            if (itemSection == null) continue;

            try {
                CustomItem item = loadItemFromConfig(itemId, itemSection);
                if (item != null) {
                    registerItem(item);
                    count++;
                }
            } catch (Exception e) {
                plugin.log(Level.WARNING, "Failed to load item " + itemId + ": " + e.getMessage());
            }
        }

        plugin.log(Level.INFO, "Loaded " + count + " custom items.");

        // Load reforges
        reforgeManager.loadReforges(config);
    }

    /**
     * Load a single item from configuration.
     */
    private CustomItem loadItemFromConfig(String itemId, ConfigurationSection section) {
        CustomItem.Builder builder = new CustomItem.Builder()
                .id(itemId)
                .displayName(section.getString("name", itemId))
                .material(section.getString("material", "STONE"))
                .category(section.getString("category", "MISC"))
                .rarity(section.getString("rarity", "COMMON"))
                .enchantGlow(section.getBoolean("enchanted", false));

        // Load skull texture
        if (section.contains("skull-texture")) {
            builder.skullTexture(section.getString("skull-texture"));
        }

        // Load leather color
        if (section.contains("leather-color")) {
            builder.leatherColor(section.getString("leather-color"));
        }

        // Load stats
        ConfigurationSection statsSection = section.getConfigurationSection("stats");
        if (statsSection != null) {
            ItemStats stats = new ItemStats();
            for (String statKey : statsSection.getKeys(false)) {
                StatType statType = StatType.fromString(statKey);
                if (statType != null) {
                    stats.setStat(statType, statsSection.getDouble(statKey));
                }
            }
            builder.stats(stats);
        }

        // Load ability
        ConfigurationSection abilitySection = section.getConfigurationSection("ability");
        if (abilitySection != null) {
            ItemAbility ability = new ItemAbility.Builder()
                    .name(abilitySection.getString("name", "Unknown"))
                    .trigger(abilitySection.getString("trigger", "RIGHT_CLICK"))
                    .description(abilitySection.getStringList("description"))
                    .cooldown(abilitySection.getInt("cooldown", 0))
                    .manaCost(abilitySection.getInt("mana-cost", 0))
                    .build();
            builder.ability(ability);
        }

        return builder.build();
    }

    /**
     * Register a custom item.
     */
    public void registerItem(CustomItem item) {
        itemRegistry.put(item.getId().toUpperCase(), item);
    }

    /**
     * Get a custom item by ID.
     */
    public CustomItem getItem(String id) {
        return itemRegistry.get(id.toUpperCase());
    }

    /**
     * Get all registered items.
     */
    public Collection<CustomItem> getAllItems() {
        return itemRegistry.values();
    }

    /**
     * Get all item IDs.
     */
    public Set<String> getItemIds() {
        return itemRegistry.keySet();
    }

    /**
     * Create an ItemStack from a custom item ID.
     */
    public ItemStack createItemStack(String itemId) {
        CustomItem item = getItem(itemId);
        if (item == null) return null;
        return item.build(itemIdKey);
    }

    /**
     * Create an ItemStack with a specific amount.
     */
    public ItemStack createItemStack(String itemId, int amount) {
        ItemStack item = createItemStack(itemId);
        if (item != null) {
            item.setAmount(amount);
        }
        return item;
    }

    /**
     * Get the custom item ID from an ItemStack.
     */
    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
    }

    /**
     * Check if an ItemStack is a custom item.
     */
    public boolean isCustomItem(ItemStack item) {
        return getItemId(item) != null;
    }

    /**
     * Get the CustomItem from an ItemStack.
     */
    public CustomItem getCustomItem(ItemStack item) {
        String id = getItemId(item);
        return id != null ? getItem(id) : null;
    }

    /**
     * Give a custom item to a player.
     */
    public boolean giveItem(Player player, String itemId) {
        return giveItem(player, itemId, 1);
    }

    /**
     * Give a custom item to a player with specific amount.
     */
    public boolean giveItem(Player player, String itemId, int amount) {
        ItemStack item = createItemStack(itemId, amount);
        if (item == null) return false;

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            // Drop remaining items
            for (ItemStack remaining : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), remaining);
            }
        }
        return true;
    }

    /**
     * Get the reforge manager.
     */
    public ReforgeManager getReforgeManager() {
        return reforgeManager;
    }

    /**
     * Get items by category.
     */
    public List<CustomItem> getItemsByCategory(ItemCategory category) {
        List<CustomItem> items = new ArrayList<>();
        for (CustomItem item : itemRegistry.values()) {
            if (item.getCategory() == category) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Get items by rarity.
     */
    public List<CustomItem> getItemsByRarity(Rarity rarity) {
        List<CustomItem> items = new ArrayList<>();
        for (CustomItem item : itemRegistry.values()) {
            if (item.getRarity() == rarity) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Reload items from configuration.
     */
    public void reload() {
        itemRegistry.clear();
        cooldowns.clear();
        loadItems();
    }

    // Event handlers for item abilities
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getModuleManager().isModuleEnabled("items")) return;
        if (!plugin.getModuleManager().isSubModuleEnabled("items", "abilities")) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        CustomItem customItem = getCustomItem(item);
        if (customItem == null || !customItem.hasAbility()) return;

        ItemAbility ability = customItem.getAbility();

        // Check trigger
        boolean triggered = false;
        if (ability.getTrigger() == AbilityTrigger.RIGHT_CLICK &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            triggered = true;
        } else if (ability.getTrigger() == AbilityTrigger.LEFT_CLICK &&
                (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            triggered = true;
        } else if (ability.getTrigger() == AbilityTrigger.SNEAK_RIGHT_CLICK &&
                player.isSneaking() &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            triggered = true;
        }

        if (!triggered) return;

        // Check cooldown
        String cooldownKey = player.getUniqueId() + ":" + customItem.getId();
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse != null && ability.hasCooldown()) {
            long remaining = (lastUse + ability.getCooldownSeconds() * 1000) - System.currentTimeMillis();
            if (remaining > 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("items.ability.cooldown")
                        .replace("{time}", String.format("%.1f", remaining / 1000.0)));
                return;
            }
        }

        // Execute ability (specific implementations would go here)
        executeAbility(player, customItem, ability);

        // Set cooldown
        if (ability.hasCooldown()) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Execute an item ability.
     */
    private void executeAbility(Player player, CustomItem item, ItemAbility ability) {
        // This is where specific ability implementations would go
        // For now, just send a message
        player.sendMessage(plugin.getConfigManager().getMessage("items.ability.activated")
                .replace("{name}", ability.getName()));

        // Specific ability implementations based on item ID
        switch (item.getId().toUpperCase()) {
            case "ASPECT_OF_THE_END":
                // Teleport forward
                player.teleport(player.getLocation().add(player.getLocation().getDirection().multiply(8)));
                break;
            case "ZOMBIE_SWORD":
                // Heal player
                double newHealth = Math.min(player.getHealth() + 10, player.getMaxHealth());
                player.setHealth(newHealth);
                break;
            // Add more ability implementations here
        }
    }
}
