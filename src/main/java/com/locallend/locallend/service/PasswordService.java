package com.locallend.locallend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Centralized password hashing and verification service.
 * Uses BCrypt for secure password storage.
 */
@Service
public class PasswordService {
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hash a raw password using BCrypt.
     * @param rawPassword Plain text password
     * @return Hashed password
     */
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verify a raw password against a hashed password.
     * @param rawPassword Plain text password
     * @param hashedPassword Hashed password from database
     * @return true if passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
