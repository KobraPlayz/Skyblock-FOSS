package com.skyblock.gui;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Abstract base class for GUIs.
 */
public abstract class AbstractGUI {

    protected final SkyblockPlugin plugin;
    protected final String title;
    protected final int size;
    protected Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers;

    public AbstractGUI(SkyblockPlugin plugin, String title, int rows) {
        this.plugin = plugin;
        this.title = ColorUtils.colorize(title);
        this.size = rows * 9;
        this.clickHandlers = new HashMap<>();
    }

    /**
     * Build the GUI contents.
     */
    protected abstract void build(Player player);

    /**
     * Open the GUI for a player.
     */
    public void open(Player player) {
        inventory = Bukkit.createInventory(new GUIManager.GUIHolder(this), size, title);
        build(player);
        player.openInventory(inventory);
    }

    /**
     * Handle a click event.
     */
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        Consumer<InventoryClickEvent> handler = clickHandlers.get(slot);

        if (handler != null) {
            playClickSound((Player) event.getWhoClicked());
            handler.accept(event);
        }
    }

    /**
     * Called when the GUI is closed.
     */
    public void onClose(Player player) {
        // Override in subclasses if needed
    }

    /**
     * Set an item in the GUI.
     */
    protected void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < size) {
            inventory.setItem(slot, item);
        }
    }

    /**
     * Set an item with a click handler.
     */
    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        setItem(slot, item);
        if (handler != null) {
            clickHandlers.put(slot, handler);
        }
    }

    /**
     * Fill empty slots with a filler item.
     */
    protected void fillEmpty(ItemStack filler) {
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    /**
     * Fill border with an item.
     */
    protected void fillBorder(ItemStack item) {
        int rows = size / 9;
        for (int i = 0; i < 9; i++) {
            setItem(i, item); // Top row
            setItem((rows - 1) * 9 + i, item); // Bottom row
        }
        for (int i = 1; i < rows - 1; i++) {
            setItem(i * 9, item); // Left column
            setItem(i * 9 + 8, item); // Right column
        }
    }

    /**
     * Create a filler glass pane.
     */
    protected ItemStack createFiller() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .build();
    }

    /**
     * Create a back button.
     */
    protected ItemStack createBackButton() {
        return new ItemBuilder(Material.ARROW)
                .name("&cBack")
                .lore("&7Click to go back")
                .build();
    }

    /**
     * Create a close button.
     */
    protected ItemStack createCloseButton() {
        return new ItemBuilder(Material.BARRIER)
                .name("&cClose")
                .lore("&7Click to close")
                .build();
    }

    /**
     * Play click sound.
     */
    protected void playClickSound(Player player) {
        if (plugin.getModuleManager().isModuleEnabled("sounds")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    /**
     * Play success sound.
     */
    protected void playSuccessSound(Player player) {
        if (plugin.getModuleManager().isModuleEnabled("sounds")) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        }
    }

    /**
     * Play error sound.
     */
    protected void playErrorSound(Player player) {
        if (plugin.getModuleManager().isModuleEnabled("sounds")) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }

    /**
     * Get the inventory.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the title.
     */
    public String getTitle() {
        return title;
    }
}
