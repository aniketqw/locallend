package com.locallend.locallend.exception;

/**
 * Exception thrown when a booking is not found by ID.
 */
public class BookingNotFoundException extends RuntimeException {
    
    private String bookingId;

    public BookingNotFoundException(String bookingId) {
        super("Booking not found with ID: " + bookingId);
        this.bookingId = bookingId;
    }

    public BookingNotFoundException(String bookingId, String message) {
        super(message);
        this.bookingId = bookingId;
    }

    public BookingNotFoundException(String bookingId, String message, Throwable cause) {
        super(message, cause);
        this.bookingId = bookingId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public static BookingNotFoundException withId(String bookingId) {
        return new BookingNotFoundException(bookingId);
    }
}
