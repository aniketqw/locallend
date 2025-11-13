package com.locallend.locallend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date/time operations related to booking management.
 */
public class DateTimeUtil {

    private DateTimeUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculate the number of days between two dates (inclusive).
     */
    public static long calculateDaysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
    }

    /**
     * Calculate the number of days between two LocalDate instances (inclusive).
     */
    public static long calculateDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    /**
     * Check if a date range overlaps with another date range.
     */
    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                       LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        LocalDate s1 = start1.toLocalDate();
        LocalDate e1 = end1.toLocalDate();
        LocalDate s2 = start2.toLocalDate();
        LocalDate e2 = end2.toLocalDate();
        
        return !s1.isAfter(e2) && !e1.isBefore(s2);
    }

    /**
     * Check if a date range overlaps with another date range (LocalDate version).
     */
    public static boolean isOverlapping(LocalDate start1, LocalDate end1,
                                       LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    /**
     * Validate that end date is after start date.
     */
    public static boolean isValidDateRange(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && end.isAfter(start);
    }

    /**
     * Validate that end date is after start date (LocalDate version).
     */
    public static boolean isValidDateRange(LocalDate start, LocalDate end) {
        return start != null && end != null && end.isAfter(start);
    }

    /**
     * Check if a date is in the future.
     */
    public static boolean isFuture(LocalDateTime date) {
        return date != null && date.isAfter(LocalDateTime.now());
    }

    /**
     * Check if a date is in the future (LocalDate version).
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Check if a date is in the past.
     */
    public static boolean isPast(LocalDateTime date) {
        return date != null && date.isBefore(LocalDateTime.now());
    }

    /**
     * Calculate days remaining until a date.
     */
    public static long daysUntil(LocalDateTime date) {
        if (date == null) return 0;
        return ChronoUnit.DAYS.between(LocalDateTime.now(), date);
    }

    /**
     * Calculate days remaining until a date (LocalDate version).
     */
    public static long daysUntil(LocalDate date) {
        if (date == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    /**
     * Check if a booking is overdue.
     */
    public static boolean isOverdue(LocalDateTime endDate) {
        return endDate != null && LocalDateTime.now().isAfter(endDate);
    }
}
