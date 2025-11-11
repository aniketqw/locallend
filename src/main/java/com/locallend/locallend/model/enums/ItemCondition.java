package com.locallend.locallend.model.enums;

public enum ItemCondition {
    NEW("Brand new"),
    EXCELLENT("Excellent condition"),
    GOOD("Good condition"),
    FAIR("Fair condition"),
    POOR("Poor condition");

    private final String description;

    ItemCondition(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public static ItemCondition fromString(String s) {
        if (s == null) return null;
        try {
            return ItemCondition.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
