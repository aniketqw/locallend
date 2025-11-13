package com.locallend.locallend.exception;

/**
 * Generic exception for unauthorized access to resources.
 * More specific than UnauthorizedItemAccessException for broader use cases.
 */
public class UnauthorizedAccessException extends RuntimeException {
    private final String userId;
    private final String resourceType;
    private final String resourceId;

    public UnauthorizedAccessException(String userId, String resourceType, String resourceId) {
        super(String.format("User '%s' is not authorized to access %s '%s'", userId, resourceType, resourceId));
        this.userId = userId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public UnauthorizedAccessException(String message) {
        super(message);
        this.userId = null;
        this.resourceType = null;
        this.resourceId = null;
    }

    public String getUserId() { 
        return userId; 
    }

    public String getResourceType() { 
        return resourceType; 
    }

    public String getResourceId() { 
        return resourceId; 
    }
}
