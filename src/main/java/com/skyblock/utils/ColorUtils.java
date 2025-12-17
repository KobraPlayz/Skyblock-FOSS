package com.skyblock.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for color code handling.
 */
public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Colorize a string with color codes and hex colors.
     */
    public static String colorize(String message) {
        if (message == null) return "";

        // Handle hex colors
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);

        // Handle standard color codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Colorize a list of strings.
     */
    public static List<String> colorize(List<String> messages) {
        List<String> colored = new ArrayList<>();
        for (String message : messages) {
            colored.add(colorize(message));
        }
        return colored;
    }

    /**
     * Strip all color codes from a string.
     */
    public static String stripColor(String message) {
        if (message == null) return "";
        return ChatColor.stripColor(colorize(message));
    }

    /**
     * Get color code for a rarity.
     */
    public static String getRarityColor(String rarity) {
        switch (rarity.toUpperCase()) {
            case "COMMON":
                return "&f";
            case "UNCOMMON":
                return "&a";
            case "RARE":
                return "&9";
            case "EPIC":
                return "&5";
            case "LEGENDARY":
                return "&6";
            case "MYTHIC":
                return "&d";
            case "DIVINE":
                return "&b";
            case "SPECIAL":
            case "VERY_SPECIAL":
                return "&c";
            default:
                return "&f";
        }
    }

    /**
     * Format a number with color based on positive/negative.
     */
    public static String formatStat(double value, boolean isPositive) {
        String prefix = isPositive ? "&a+" : "&c";
        return prefix + NumberUtils.format(value);
    }

    /**
     * Create a progress bar string.
     */
    public static String createProgressBar(double current, double max, int length, String filledColor, String emptyColor) {
        double percentage = current / max;
        int filled = (int) Math.round(percentage * length);

        StringBuilder bar = new StringBuilder();
        bar.append(filledColor);

        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("▌");
            } else {
                if (i == filled) {
                    bar.append(emptyColor);
                }
                bar.append("▌");
            }
        }

        return colorize(bar.toString());
    }

    /**
     * Create a simple dash progress bar.
     */
    public static String createDashProgressBar(double current, double max, int length) {
        double percentage = Math.min(1.0, current / max);
        int filled = (int) Math.round(percentage * length);

        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("-");
            } else {
                if (i == filled) {
                    bar.append("&f");
                }
                bar.append("-");
            }
        }

        return colorize(bar.toString());
    }
}
