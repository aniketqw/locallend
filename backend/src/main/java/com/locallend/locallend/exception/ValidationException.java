package com.locallend.locallend.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception for custom validation errors that don't fit into Bean Validation.
 * Can carry multiple field-level error messages.
 */
public class ValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }

    public ValidationException(String field, String error) {
        super("Validation failed");
        this.fieldErrors = new HashMap<>();
        this.fieldErrors.put(field, error);
    }

    public Map<String, String> getFieldErrors() { 
        return fieldErrors; 
    }

    public void addFieldError(String field, String error) {
        this.fieldErrors.put(field, error);
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
