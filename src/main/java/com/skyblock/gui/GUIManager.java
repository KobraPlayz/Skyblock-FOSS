package com.skyblock.gui;

import com.skyblock.SkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages GUI instances and events.
 */
public class GUIManager implements Listener {

    private final SkyblockPlugin plugin;
    private final Map<UUID, AbstractGUI> openGuis;

    public GUIManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
    }

    /**
     * Open a GUI for a player.
     */
    public void openGUI(Player player, AbstractGUI gui) {
        openGuis.put(player.getUniqueId(), gui);
        gui.open(player);
    }

    /**
     * Get the GUI a player has open.
     */
    public AbstractGUI getOpenGUI(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    /**
     * Close all open GUIs.
     */
    public void closeAll() {
        for (UUID uuid : openGuis.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.closeInventory();
            }
        }
        openGuis.clear();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        AbstractGUI gui = openGuis.get(player.getUniqueId());

        if (gui == null) return;

        // Check if clicking in the GUI inventory
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        // Cancel if clicking in GUI
        if (clickedInv.getHolder() instanceof GUIHolder) {
            event.setCancelled(true);
            gui.handleClick(event);
        } else if (event.getClick().isShiftClick()) {
            // Cancel shift-clicks to prevent moving items
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        AbstractGUI gui = openGuis.remove(player.getUniqueId());

        if (gui != null) {
            gui.onClose(player);
        }
    }

    /**
     * Marker interface for GUI holders.
     */
    public static class GUIHolder implements InventoryHolder {
        private final AbstractGUI gui;

        public GUIHolder(AbstractGUI gui) {
            this.gui = gui;
        }

        public AbstractGUI getGui() {
            return gui;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
