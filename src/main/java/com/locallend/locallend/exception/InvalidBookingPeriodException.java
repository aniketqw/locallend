package com.locallend.locallend.exception;

/**
 * Exception thrown when a booking period violates rental period constraints.
 */
public class InvalidBookingPeriodException extends RuntimeException {
    
    private Integer minDays;
    private Integer maxDays;
    private Long requestedDays;

    public InvalidBookingPeriodException(String message) {
        super(message);
    }

    public InvalidBookingPeriodException(String message, Integer minDays, Integer maxDays, Long requestedDays) {
        super(message);
        this.minDays = minDays;
        this.maxDays = maxDays;
        this.requestedDays = requestedDays;
    }

    public InvalidBookingPeriodException(String message, Throwable cause) {
        super(message, cause);
    }

    public Integer getMinDays() {
        return minDays;
    }

    public Integer getMaxDays() {
        return maxDays;
    }

    public Long getRequestedDays() {
        return requestedDays;
    }
}
