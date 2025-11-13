package com.locallend.locallend.exception;

public class CategoryNotFoundException extends RuntimeException {

    private final String categoryId;
    private final String categoryName;

    public CategoryNotFoundException(String categoryId) {
        super("Category with ID '" + categoryId + "' not found");
        this.categoryId = categoryId;
        this.categoryName = null;
    }

    public CategoryNotFoundException(String categoryName, boolean byName) {
        super("Category with name '" + categoryName + "' not found");
        this.categoryId = null;
        this.categoryName = categoryName;
    }

    public CategoryNotFoundException(String message, String categoryId, String categoryName) {
        super(message);
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public static CategoryNotFoundException byId(String categoryId) {
        return new CategoryNotFoundException(categoryId);
    }

    public static CategoryNotFoundException byName(String categoryName) {
        return new CategoryNotFoundException(categoryName, true);
    }

    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
}
