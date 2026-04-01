package com.tuyensinh.util;

public class StringUtil {

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String trimToNull(String str) {
        if (str == null) return null;
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s == null ? "" : s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s == null ? "" : s);
    }
}
