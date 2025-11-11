package com.locallend.locallend.exception;

/**
 * Exception thrown when an operation cannot be performed due to the current state.
 * Examples: Cannot delete active booking, cannot modify completed booking, etc.
 */
public class InvalidOperationException extends RuntimeException {
    private final String operation;
    private final String reason;

    public InvalidOperationException(String operation, String reason) {
        super(String.format("Cannot perform operation '%s': %s", operation, reason));
        this.operation = operation;
        this.reason = reason;
    }

    public InvalidOperationException(String message) {
        super(message);
        this.operation = null;
        this.reason = null;
    }

    public String getOperation() { 
        return operation; 
    }

    public String getReason() { 
        return reason; 
    }
}
