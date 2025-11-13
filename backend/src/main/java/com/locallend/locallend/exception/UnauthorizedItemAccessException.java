package com.locallend.locallend.exception;

public class UnauthorizedItemAccessException extends RuntimeException {
    public UnauthorizedItemAccessException(String message) { super(message); }
    public UnauthorizedItemAccessException() { super("Unauthorized access to item"); }
}
