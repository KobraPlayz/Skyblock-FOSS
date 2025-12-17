package com.skyblock.api;

import com.skyblock.SkyblockPlugin;
import com.skyblock.collections.Collection;
import com.skyblock.collections.CollectionManager;
import com.skyblock.economy.EconomyManager;
import com.skyblock.items.CustomItem;
import com.skyblock.items.ItemManager;
import com.skyblock.player.PlayerManager;
import com.skyblock.player.SkyblockPlayer;
import com.skyblock.skills.SkillManager;
import com.skyblock.skills.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Public API for other plugins to interact with SkyblockFOSS.
 *
 * Example usage:
 * SkyblockAPI api = SkyblockPlugin.getInstance().getAPI();
 * double coins = api.getCoins(player);
 */
public class SkyblockAPI {

    private final SkyblockPlugin plugin;

    public SkyblockAPI(SkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== Player API ====================

    /**
     * Get a SkyblockPlayer instance.
     */
    public SkyblockPlayer getPlayer(Player player) {
        return plugin.getPlayerManager().getPlayer(player);
    }

    /**
     * Get a SkyblockPlayer instance by UUID.
     */
    public SkyblockPlayer getPlayer(UUID uuid) {
        return plugin.getPlayerManager().getPlayer(uuid);
    }

    // ==================== Economy API ====================

    /**
     * Get a player's coin balance.
     */
    public double getCoins(Player player) {
        return plugin.getEconomyManager().getBalance(player);
    }

    /**
     * Add coins to a player.
     */
    public boolean addCoins(Player player, double amount, String reason) {
        return plugin.getEconomyManager().addCoins(player, amount, reason);
    }

    /**
     * Remove coins from a player.
     */
    public boolean removeCoins(Player player, double amount, String reason) {
        return plugin.getEconomyManager().removeCoins(player, amount, reason);
    }

    /**
     * Check if player has enough coins.
     */
    public boolean hasCoins(Player player, double amount) {
        return plugin.getEconomyManager().hasBalance(player, amount);
    }

    // ==================== Skills API ====================

    /**
     * Get a player's skill level.
     */
    public int getSkillLevel(Player player, String skill) {
        SkyblockPlayer sbPlayer = getPlayer(player);
        return sbPlayer != null ? sbPlayer.getSkillLevel(skill) : 0;
    }

    /**
     * Get a player's skill XP.
     */
    public double getSkillXp(Player player, String skill) {
        SkyblockPlayer sbPlayer = getPlayer(player);
        return sbPlayer != null ? sbPlayer.getSkillXp(skill) : 0;
    }

    /**
     * Add skill XP to a player.
     */
    public void addSkillXp(Player player, String skill, double xp) {
        SkyblockPlayer sbPlayer = getPlayer(player);
        if (sbPlayer != null) {
            sbPlayer.addSkillXp(skill, xp);
        }
    }

    /**
     * Get the XP required for a skill level.
     */
    public long getXpForLevel(int level) {
        return plugin.getSkillManager().getXpForLevel(level);
    }

    /**
     * Get all skill types.
     */
    public SkillType[] getSkillTypes() {
        return SkillType.values();
    }

    // ==================== Collections API ====================

    /**
     * Get a player's collection amount.
     */
    public long getCollectionAmount(Player player, String collection) {
        SkyblockPlayer sbPlayer = getPlayer(player);
        return sbPlayer != null ? sbPlayer.getCollectionAmount(collection) : 0;
    }

    /**
     * Get a player's collection tier.
     */
    public int getCollectionTier(Player player, String collection) {
        SkyblockPlayer sbPlayer = getPlayer(player);
        return sbPlayer != null ? sbPlayer.getCollectionTier(collection) : 0;
    }

    /**
     * Add to a player's collection.
     */
    public void addCollection(Player player, String collection, long amount) {
        SkyblockPlayer sbPlayer = getPlayer(player);
        if (sbPlayer != null) {
            sbPlayer.addCollection(collection, amount);
        }
    }

    /**
     * Get a collection by ID.
     */
    public Collection getCollection(String id) {
        return plugin.getCollectionManager().getCollection(id);
    }

    // ==================== Items API ====================

    /**
     * Get a custom item by ID.
     */
    public CustomItem getCustomItem(String id) {
        return plugin.getItemManager().getItem(id);
    }

    /**
     * Create an ItemStack from a custom item.
     */
    public ItemStack createItemStack(String itemId) {
        return plugin.getItemManager().createItemStack(itemId);
    }

    /**
     * Create an ItemStack with amount.
     */
    public ItemStack createItemStack(String itemId, int amount) {
        return plugin.getItemManager().createItemStack(itemId, amount);
    }

    /**
     * Give a custom item to a player.
     */
    public boolean giveItem(Player player, String itemId) {
        return plugin.getItemManager().giveItem(player, itemId);
    }

    /**
     * Give a custom item with amount.
     */
    public boolean giveItem(Player player, String itemId, int amount) {
        return plugin.getItemManager().giveItem(player, itemId, amount);
    }

    /**
     * Check if an ItemStack is a custom item.
     */
    public boolean isCustomItem(ItemStack item) {
        return plugin.getItemManager().isCustomItem(item);
    }

    /**
     * Get the custom item ID from an ItemStack.
     */
    public String getItemId(ItemStack item) {
        return plugin.getItemManager().getItemId(item);
    }

    // ==================== Module API ====================

    /**
     * Check if a module is enabled.
     */
    public boolean isModuleEnabled(String module) {
        return plugin.getModuleManager().isModuleEnabled(module);
    }

    /**
     * Check if a sub-module is enabled.
     */
    public boolean isSubModuleEnabled(String parent, String child) {
        return plugin.getModuleManager().isSubModuleEnabled(parent, child);
    }

    // ==================== Manager Access ====================

    /**
     * Get the PlayerManager.
     */
    public PlayerManager getPlayerManager() {
        return plugin.getPlayerManager();
    }

    /**
     * Get the EconomyManager.
     */
    public EconomyManager getEconomyManager() {
        return plugin.getEconomyManager();
    }

    /**
     * Get the SkillManager.
     */
    public SkillManager getSkillManager() {
        return plugin.getSkillManager();
    }

    /**
     * Get the CollectionManager.
     */
    public CollectionManager getCollectionManager() {
        return plugin.getCollectionManager();
    }

    /**
     * Get the ItemManager.
     */
    public ItemManager getItemManager() {
        return plugin.getItemManager();
    }
}
