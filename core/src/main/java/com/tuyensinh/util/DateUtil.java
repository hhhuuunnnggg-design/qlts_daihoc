package com.tuyensinh.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr.trim(), DATE_DISPLAY);
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.parse(dateStr.trim(), DATE_SHORT);
                } catch (DateTimeParseException e3) {
                    return null;
                }
            }
        }
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_DISPLAY);
    }

    /** Hien thi ngan: dd/MM/yy */
    public static String formatDateShort(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_SHORT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DATETIME_FMT);
    }

    public static String toDbString(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FMT);
    }
}
