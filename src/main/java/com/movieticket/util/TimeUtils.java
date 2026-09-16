package com.movieticket.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * TimeUtils.java — Utility for parsing movie showtimes, durations, and computing Expected End Time.
 */
public class TimeUtils {

    private static final DateTimeFormatter FORMATTER_12H = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter FORMATTER_12H_ALT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter FORMATTER_24H = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    /**
     * Calculates expected end time string for a given show start time and movie duration in minutes.
     * Example: ("07:30 PM", 148) -> "09:58 PM"
     */
    public static String calculateEndTime(String startTimeStr, int durationMinutes) {
        if (startTimeStr == null || startTimeStr.trim().isEmpty()) {
            return "—";
        }
        if (durationMinutes <= 0) {
            durationMinutes = 120; // Default fallback: 2 hours
        }

        LocalTime startTime = parseStartTime(startTimeStr.trim());
        if (startTime == null) {
            return "—";
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        return endTime.format(FORMATTER_12H);
    }

    /**
     * Parses duration string (e.g. "148 mins", "2h 28m", "148") into integer minutes.
     */
    public static int parseDurationMinutes(String durationStr) {
        if (durationStr == null || durationStr.trim().isEmpty()) {
            return 120;
        }
        String clean = durationStr.toLowerCase().trim();
        try {
            if (clean.contains("h")) {
                int hours = 0;
                int mins = 0;
                String[] parts = clean.split("h");
                hours = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                if (parts.length > 1) {
                    mins = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                }
                return (hours * 60) + mins;
            }
            return Integer.parseInt(clean.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 120;
        }
    }

    private static LocalTime parseStartTime(String str) {
        try {
            return LocalTime.parse(str, FORMATTER_12H);
        } catch (DateTimeParseException ignored) {}

        try {
            return LocalTime.parse(str, FORMATTER_12H_ALT);
        } catch (DateTimeParseException ignored) {}

        try {
            return LocalTime.parse(str, FORMATTER_24H);
        } catch (DateTimeParseException ignored) {}

        return null;
    }
}
