package com.skyblock.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Utility class for particle effects.
 */
public final class ParticleUtils {

    private ParticleUtils() {
        // Utility class
    }

    /**
     * Display a skill XP gain effect.
     * @param player The player to show to
     */
    public static void playSkillXpEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.spawnParticle(Particle.HAPPY_VILLAGER, loc, 5, 0.3, 0.3, 0.3, 0);
    }

    /**
     * Display a level up effect.
     * @param player The player to show around
     */
    public static void playLevelUpEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 0.5, 1.0, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.FIREWORK, loc, 20, 0.5, 1.0, 0.5, 0.1);
    }

    /**
     * Display a collection unlock effect.
     * @param player The player to show to
     */
    public static void playCollectionUnlockEffect(Player player) {
        Location loc = player.getLocation().add(0, 2, 0);
        player.getWorld().spawnParticle(Particle.END_ROD, loc, 15, 0.5, 0.2, 0.5, 0.05);
    }

    /**
     * Display coin pickup particles.
     * @param location The location to spawn at
     */
    public static void playCoinPickupEffect(Location location) {
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 8, 0.2, 0.2, 0.2, 0);
        }
    }

    /**
     * Display an ability activation effect.
     * @param player The player who activated the ability
     */
    public static void playAbilityEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.WITCH, loc, 15, 0.4, 0.5, 0.4, 0);
    }

    /**
     * Display a damage indicator particle.
     * @param location The location of the hit
     * @param isCritical Whether it was a critical hit
     */
    public static void playDamageIndicator(Location location, boolean isCritical) {
        if (location.getWorld() == null) return;

        if (isCritical) {
            location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 10, 0.2, 0.3, 0.2, 0.1);
        } else {
            location.getWorld().spawnParticle(Particle.CRIT, location, 5, 0.2, 0.2, 0.2, 0.05);
        }
    }

    /**
     * Create a spiral effect around a player.
     * @param player The player to create the spiral around
     * @param particle The particle to use
     * @param height Total height of the spiral
     * @param radius Radius of the spiral
     * @param points Number of points in the spiral
     */
    public static void createSpiral(Player player, Particle particle, double height, double radius, int points) {
        Location center = player.getLocation();

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points * 3; // 3 rotations
            double y = (height * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location point = center.clone().add(x, y, z);
            player.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Create a circle of particles.
     * @param center Center of the circle
     * @param particle The particle to use
     * @param radius Radius of the circle
     * @param points Number of points in the circle
     */
    public static void createCircle(Location center, Particle particle, double radius, int points) {
        if (center.getWorld() == null) return;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location point = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Create a beam of particles between two locations.
     * @param start Start location
     * @param end End location
     * @param particle The particle to use
     * @param density Particles per block
     */
    public static void createBeam(Location start, Location end, Particle particle, double density) {
        if (start.getWorld() == null) return;

        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        int points = (int) (distance * density);

        for (int i = 0; i < points; i++) {
            Location point = start.clone().add(direction.clone().multiply((double) i / density));
            start.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Display colored dust particles.
     * @param location The location to spawn at
     * @param red Red component (0-255)
     * @param green Green component (0-255)
     * @param blue Blue component (0-255)
     * @param count Number of particles
     */
    public static void spawnColoredDust(Location location, int red, int green, int blue, int count) {
        if (location.getWorld() == null) return;

        Particle.DustOptions dust = new Particle.DustOptions(
                Color.fromRGB(red, green, blue), 1.0f
        );
        location.getWorld().spawnParticle(Particle.DUST, location, count, 0.2, 0.2, 0.2, 0, dust);
    }

    /**
     * Display rarity-based particle effect.
     * @param location The location
     * @param rarityColor The hex color code of the rarity
     */
    public static void playRarityParticle(Location location, String rarityColor) {
        if (location.getWorld() == null) return;

        Color color = parseColor(rarityColor);
        if (color == null) return;

        Particle.DustOptions dust = new Particle.DustOptions(color, 1.2f);
        location.getWorld().spawnParticle(Particle.DUST, location, 10, 0.3, 0.3, 0.3, 0, dust);
    }

    /**
     * Parse a hex color string to a Bukkit Color.
     * @param hex Hex color string (e.g., "#FF5555" or "FF5555")
     * @return The Color, or null if invalid
     */
    private static Color parseColor(String hex) {
        if (hex == null) return null;

        hex = hex.replace("#", "").replace("&", "");
        if (hex.length() != 6) return null;

        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
