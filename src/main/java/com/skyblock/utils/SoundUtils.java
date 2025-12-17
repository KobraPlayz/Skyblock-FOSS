package com.skyblock.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Utility class for playing sounds.
 */
public final class SoundUtils {

    private SoundUtils() {
        // Utility class
    }

    // Common sound presets
    public static void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    public static void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
    }

    public static void playClick(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    public static void playMenuOpen(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    }

    public static void playMenuClose(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.2f);
    }

    public static void playLevelUp(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    public static void playSkillXp(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 2.0f);
    }

    public static void playCollectionUnlock(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public static void playCoinPickup(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
    }

    public static void playCoinSpend(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.5f);
    }

    public static void playItemAbility(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 2.0f);
    }

    public static void playAbilityCooldown(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    public static void playRareItemObtain(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    public static void playProfileSwitch(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.3f, 1.5f);
    }

    public static void playWarning(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
    }

    public static void playConfirm(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }

    /**
     * Play a custom sound at a specific location.
     * @param location The location to play at
     * @param sound The sound to play
     * @param volume Volume (0.0 - 1.0)
     * @param pitch Pitch (0.5 - 2.0)
     */
    public static void playAt(Location location, Sound sound, float volume, float pitch) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    /**
     * Play a sound from a config string.
     * @param player The player to play the sound for
     * @param soundString Sound in format "SOUND_NAME:volume:pitch" or just "SOUND_NAME"
     */
    public static void playFromConfig(Player player, String soundString) {
        if (soundString == null || soundString.isEmpty() || soundString.equalsIgnoreCase("none")) {
            return;
        }

        String[] parts = soundString.split(":");
        Sound sound;

        try {
            sound = Sound.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
        float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
