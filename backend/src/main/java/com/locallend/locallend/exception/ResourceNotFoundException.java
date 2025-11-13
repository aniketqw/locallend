package com.locallend.locallend.exception;

/**
 * Generic exception for any resource not found.
 * Can be used as a base class or directly when resource type doesn't need specific handling.
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with ID '%s' not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public String getResourceType() { 
        return resourceType; 
    }

    public String getResourceId() { 
        return resourceId; 
    }
}
