package com.skyblock.utils;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for time formatting and calculations.
 */
public final class TimeUtils {

    private TimeUtils() {
        // Utility class
    }

    /**
     * Format milliseconds into a human-readable string.
     * @param millis Time in milliseconds
     * @return Formatted string (e.g., "2h 30m 15s")
     */
    public static String formatDuration(long millis) {
        if (millis < 1000) {
            return "0s";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 || builder.length() == 0) {
            builder.append(seconds).append("s");
        }

        return builder.toString().trim();
    }

    /**
     * Format milliseconds into a compact string.
     * @param millis Time in milliseconds
     * @return Compact string (e.g., "2:30:15")
     */
    public static String formatCompact(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    /**
     * Format seconds into a countdown string.
     * @param seconds Time in seconds
     * @return Countdown string (e.g., "5:00")
     */
    public static String formatCountdown(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    /**
     * Get current timestamp in milliseconds.
     * @return Current system time in milliseconds
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Check if a cooldown has expired.
     * @param lastUse Last use timestamp in milliseconds
     * @param cooldownMs Cooldown duration in milliseconds
     * @return True if cooldown has expired
     */
    public static boolean hasCooldownExpired(long lastUse, long cooldownMs) {
        return now() - lastUse >= cooldownMs;
    }

    /**
     * Get remaining cooldown time in milliseconds.
     * @param lastUse Last use timestamp in milliseconds
     * @param cooldownMs Cooldown duration in milliseconds
     * @return Remaining time in milliseconds, 0 if expired
     */
    public static long getRemainingCooldown(long lastUse, long cooldownMs) {
        long remaining = cooldownMs - (now() - lastUse);
        return Math.max(0, remaining);
    }

    /**
     * Parse a duration string into milliseconds.
     * Supports formats like "1d", "2h", "30m", "45s", "1d2h30m"
     * @param input Duration string
     * @return Duration in milliseconds, or -1 if invalid
     */
    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }

        long total = 0;
        StringBuilder number = new StringBuilder();

        for (char c : input.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (number.length() > 0) {
                long value = Long.parseLong(number.toString());
                number.setLength(0);

                switch (c) {
                    case 'd':
                        total += TimeUnit.DAYS.toMillis(value);
                        break;
                    case 'h':
                        total += TimeUnit.HOURS.toMillis(value);
                        break;
                    case 'm':
                        total += TimeUnit.MINUTES.toMillis(value);
                        break;
                    case 's':
                        total += TimeUnit.SECONDS.toMillis(value);
                        break;
                    default:
                        return -1;
                }
            }
        }

        return total > 0 ? total : -1;
    }
}
