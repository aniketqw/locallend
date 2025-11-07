package com.locallend.locallend.exception;

/**
 * Exception thrown when business rules are violated.
 * Examples: duplicate user, inactive user, invalid trust score, etc.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
