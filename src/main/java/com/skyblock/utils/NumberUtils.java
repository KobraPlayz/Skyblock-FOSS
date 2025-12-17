package com.skyblock.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Utility class for number formatting and calculations.
 */
public class NumberUtils {

    private static final NavigableMap<Long, String> SUFFIXES = new TreeMap<>();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.#");
    private static final DecimalFormat COIN_FORMAT = new DecimalFormat("#,##0");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    static {
        SUFFIXES.put(1_000L, "k");
        SUFFIXES.put(1_000_000L, "M");
        SUFFIXES.put(1_000_000_000L, "B");
        SUFFIXES.put(1_000_000_000_000L, "T");
        SUFFIXES.put(1_000_000_000_000_000L, "Q");
    }

    /**
     * Format a number with abbreviations (1000 -> 1k).
     */
    public static String formatAbbreviated(double value) {
        if (value < 0) return "-" + formatAbbreviated(-value);
        if (value < 1000) return DECIMAL_FORMAT.format(value);

        long longValue = (long) value;
        NavigableMap.Entry<Long, String> entry = SUFFIXES.floorEntry(longValue);
        if (entry == null) return DECIMAL_FORMAT.format(value);

        Long divideBy = entry.getKey();
        String suffix = entry.getValue();

        double divided = value / divideBy;
        return DECIMAL_FORMAT.format(divided) + suffix;
    }

    /**
     * Format a number with commas.
     */
    public static String format(double value) {
        if (value == (long) value) {
            return NUMBER_FORMAT.format((long) value);
        }
        return DECIMAL_FORMAT.format(value);
    }

    /**
     * Format coins.
     */
    public static String formatCoins(double coins) {
        return COIN_FORMAT.format(coins);
    }

    /**
     * Format coins with abbreviation if too large.
     */
    public static String formatCoinsAbbreviated(double coins) {
        if (coins >= 1_000_000) {
            return formatAbbreviated(coins);
        }
        return formatCoins(coins);
    }

    /**
     * Format a percentage (0-1 to 0-100%).
     */
    public static String formatPercentage(double value) {
        return DECIMAL_FORMAT.format(value * 100) + "%";
    }

    /**
     * Format a percentage already in 0-100 range.
     */
    public static String formatPercent(double value) {
        return DECIMAL_FORMAT.format(value) + "%";
    }

    /**
     * Parse a string to double, returning default if invalid.
     */
    public static double parseDouble(String s, double defaultValue) {
        try {
            return Double.parseDouble(s.replace(",", "").replace("k", "000").replace("m", "000000"));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse a string to int, returning default if invalid.
     */
    public static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.replace(",", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse a string to long, returning default if invalid.
     */
    public static long parseLong(String s, long defaultValue) {
        try {
            return Long.parseLong(s.replace(",", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp an int value between min and max.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Calculate percentage progress.
     */
    public static double calculateProgress(double current, double max) {
        if (max <= 0) return 0;
        return Math.min(1.0, current / max);
    }

    /**
     * Convert roman numerals to integer.
     */
    public static int romanToInt(String roman) {
        if (roman == null || roman.isEmpty()) return 0;

        int result = 0;
        int prev = 0;

        for (int i = roman.length() - 1; i >= 0; i--) {
            int current = romanCharToInt(roman.charAt(i));
            if (current < prev) {
                result -= current;
            } else {
                result += current;
            }
            prev = current;
        }

        return result;
    }

    private static int romanCharToInt(char c) {
        switch (Character.toUpperCase(c)) {
            case 'I': return 1;
            case 'V': return 5;
            case 'X': return 10;
            case 'L': return 50;
            case 'C': return 100;
            case 'D': return 500;
            case 'M': return 1000;
            default: return 0;
        }
    }

    /**
     * Convert integer to roman numerals.
     */
    public static String intToRoman(int num) {
        if (num <= 0) return "";

        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        return thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

    /**
     * Check if two doubles are approximately equal.
     */
    public static boolean approximatelyEqual(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    /**
     * Round to specified decimal places.
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        return Math.round(value * factor) / (double) factor;
    }
}
