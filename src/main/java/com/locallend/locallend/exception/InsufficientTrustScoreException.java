package com.locallend.locallend.exception;

/**
 * Exception thrown when a user's trust score is too low to perform an operation.
 * Example: Cannot create booking with trust score below threshold.
 */
public class InsufficientTrustScoreException extends RuntimeException {
    private final String userId;
    private final Double currentScore;
    private final Double requiredScore;

    public InsufficientTrustScoreException(String userId, Double currentScore, Double requiredScore) {
        super(String.format("User '%s' has insufficient trust score. Current: %.2f, Required: %.2f", 
                userId, currentScore, requiredScore));
        this.userId = userId;
        this.currentScore = currentScore;
        this.requiredScore = requiredScore;
    }

    public String getUserId() { 
        return userId; 
    }

    public Double getCurrentScore() { 
        return currentScore; 
    }

    public Double getRequiredScore() { 
        return requiredScore; 
    }
}
