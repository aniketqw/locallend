package com.locallend.locallend.exception;

/**
 * Exception thrown when attempting an invalid state transition.
 * Example: Cannot transition booking from COMPLETED to PENDING.
 */
public class InvalidStateTransitionException extends RuntimeException {
    private final String resourceType;
    private final String currentState;
    private final String targetState;

    public InvalidStateTransitionException(String resourceType, String currentState, String targetState) {
        super(String.format("Cannot transition %s from '%s' to '%s'", resourceType, currentState, targetState));
        this.resourceType = resourceType;
        this.currentState = currentState;
        this.targetState = targetState;
    }

    public InvalidStateTransitionException(String message) {
        super(message);
        this.resourceType = null;
        this.currentState = null;
        this.targetState = null;
    }

    public String getResourceType() { 
        return resourceType; 
    }

    public String getCurrentState() { 
        return currentState; 
    }

    public String getTargetState() { 
        return targetState; 
    }
}
