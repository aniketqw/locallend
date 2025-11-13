package com.locallend.locallend.model.enums;

public enum ItemStatus {
    AVAILABLE,
    BOOKED,
    BORROWED,
    MAINTENANCE,
    UNAVAILABLE;

    public static ItemStatus fromString(String s) {
        if (s == null) return null;
        try {
            return ItemStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
