package com.locallend.locallend.exception;

/**
 * Exception thrown when an item is not available for booking.
 * Example: Item status is RENTED, UNAVAILABLE, or under maintenance.
 */
public class ItemNotAvailableException extends RuntimeException {
    private final String itemId;
    private final String currentStatus;

    public ItemNotAvailableException(String itemId, String currentStatus) {
        super(String.format("Item '%s' is not available for booking. Current status: %s", itemId, currentStatus));
        this.itemId = itemId;
        this.currentStatus = currentStatus;
    }

    public ItemNotAvailableException(String message) {
        super(message);
        this.itemId = null;
        this.currentStatus = null;
    }

    public String getItemId() { 
        return itemId; 
    }

    public String getCurrentStatus() { 
        return currentStatus; 
    }
}
