package com.locallend.locallend.exception;

/**
 * Exception thrown when a rating is not found in the system.
 */
public class RatingNotFoundException extends RuntimeException {
    private final String ratingId;

    public RatingNotFoundException(String ratingId) {
        super("Rating with ID '" + ratingId + "' not found");
        this.ratingId = ratingId;
    }

    public RatingNotFoundException(String ratingId, String message) {
        super(message);
        this.ratingId = ratingId;
    }

    public String getRatingId() { 
        return ratingId; 
    }
}
