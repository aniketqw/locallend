package com.locallend.locallend.model.enums;

/**
 * Enumeration representing the lifecycle status of a booking in LocalLend platform.
 * Tracks the progression from initial request to completion or cancellation.
 */
public enum BookingStatus {
    PENDING("Pending - Waiting for owner approval"),
    CONFIRMED("Confirmed - Approved by owner, awaiting pickup"),
    ACTIVE("Active - Item currently borrowed"),
    COMPLETED("Completed - Item returned successfully"),
    CANCELLED("Cancelled - Booking cancelled by borrower"),
    REJECTED("Rejected - Declined by owner"),
    OVERDUE("Overdue - Return date passed");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED;
    }

    public boolean canBeConfirmed() {
        return this == PENDING;
    }

    public boolean canBeActivated() {
        return this == CONFIRMED;
    }

    public boolean canBeCompleted() {
        return this == ACTIVE || this == OVERDUE;
    }

    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED || this == REJECTED;
    }

    public boolean isActive() {
        return this == ACTIVE || this == OVERDUE;
    }

    /**
     * Check if this status can transition to another status.
     */
    public boolean canTransitionTo(BookingStatus newStatus) {
        if (newStatus == null) return false;
        
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED || newStatus == REJECTED;
            case CONFIRMED -> newStatus == ACTIVE || newStatus == CANCELLED;
            case ACTIVE -> newStatus == COMPLETED || newStatus == OVERDUE;
            case COMPLETED, CANCELLED, REJECTED, OVERDUE -> false;
        };
    }

    public static BookingStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking status cannot be null or empty");
        }

        try {
            return BookingStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status: " + value +
                    ". Valid values are: PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED, REJECTED, OVERDUE");
        }
    }
}
