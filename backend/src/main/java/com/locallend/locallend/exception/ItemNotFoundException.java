package com.locallend.locallend.exception;

public class ItemNotFoundException extends RuntimeException {
    private final String itemId;

    public ItemNotFoundException(String itemId) {
        super("Item with ID '" + itemId + "' not found");
        this.itemId = itemId;
    }

    public String getItemId() { return itemId; }
}
