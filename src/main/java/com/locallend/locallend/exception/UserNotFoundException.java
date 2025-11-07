package com.locallend.locallend.exception;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {
    private final String userId;

    public UserNotFoundException(String userId) {
        super("User with ID '" + userId + "' not found");
        this.userId = userId;
    }

    public UserNotFoundException(String userId, String message) {
        super(message);
        this.userId = userId;
    }

    public String getUserId() { 
        return userId; 
    }
}
