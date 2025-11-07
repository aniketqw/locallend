package com.locallend.locallend.exception;

/**
 * Exception thrown when a booking request conflicts with existing bookings or availability constraints.
 */
public class BookingConflictException extends RuntimeException {
    
    private String itemId;
    private String conflictingBookingId;

    public BookingConflictException(String message) {
        super(message);
    }

    public BookingConflictException(String message, String itemId) {
        super(message);
        this.itemId = itemId;
    }

    public BookingConflictException(String message, String itemId, String conflictingBookingId) {
        super(message);
        this.itemId = itemId;
        this.conflictingBookingId = conflictingBookingId;
    }

    public BookingConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getItemId() {
        return itemId;
    }

    public String getConflictingBookingId() {
        return conflictingBookingId;
    }
}
