package com.locallend.locallend.service;

import com.locallend.locallend.dto.request.UserRegistrationDto;
import com.locallend.locallend.exception.BusinessException;
import com.locallend.locallend.model.User;
import com.locallend.locallend.repository.UserRepository;
import com.locallend.locallend.service.dto.UserStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Main service for user-related business logic.
 * Handles registration, authentication, profile management, and trust score updates.
 */
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Register a new user.
     * @param dto User registration data
     * @return Created user
     * @throws BusinessException if username or email already exists
     */
    public User registerUser(UserRegistrationDto dto) {
        // Check for duplicate username
        userRepository.findByUsername(dto.getUsername())
                .ifPresent(u -> { throw new BusinessException("Username already exists"); });

        // Check for duplicate email
        userRepository.findByEmail(dto.getEmail())
                .ifPresent(u -> { throw new BusinessException("Email already exists"); });

        // Create new user with hashed password
        User user = new User(dto.getUsername(), dto.getName(), dto.getEmail(),
                passwordService.hash(dto.getPassword()));
        user.setCreatedDate(LocalDateTime.now());
        user.setIsActive(true);
        user.setRole("USER");
        user.setTrustScore(5.0);  // Default trust score

        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        return userRepository.save(user);
    }

    /**
     * Verify user credentials for authentication.
     * @param usernameOrEmail Username or email
     * @param rawPassword Plain text password
     * @return Authenticated user
     * @throws BusinessException if credentials are invalid or user is inactive
     */
    public User verifyCredentials(String usernameOrEmail, String rawPassword) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException("User account is inactive");
        }

        if (!passwordService.matches(rawPassword, user.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        return user;
    }

    /**
     * Update user profile information.
     * @param userId User ID
     * @param name New name (optional)
     * @param phoneNumber New phone number (optional)
     * @param profileImageUrl New profile image URL (optional)
     * @return Updated user
     * @throws BusinessException if user not found
     */
    public User updateProfile(String userId, String name, String phoneNumber, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            user.setPhoneNumber(phoneNumber);
        }
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            user.setProfileImageUrl(profileImageUrl);
        }

        return userRepository.save(user);
    }

    /**
     * Update user trust score.
     * @param userId User ID
     * @param newTrustScore New trust score (0.0 - 5.0)
     * @throws BusinessException if trust score is out of bounds or user not found
     */
    public void updateTrustScore(String userId, double newTrustScore) {
        if (newTrustScore < 0.0 || newTrustScore > 5.0) {
            throw new BusinessException("Trust score must be between 0.0 and 5.0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        user.setTrustScore(newTrustScore);
        userRepository.save(user);
    }

    /**
     * Activate or deactivate a user account.
     * @param userId User ID
     * @param active true to activate, false to deactivate
     * @throws BusinessException if user not found
     */
    public void setActive(String userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        user.setIsActive(active);
        userRepository.save(user);
    }

    /**
     * Get aggregate user statistics.
     * @return User statistics DTO
     */
    public UserStatsDto getUserStats() {
        long total = userRepository.count();
        long active = Optional.ofNullable(userRepository.countByIsActiveTrue()).orElse(0L);
        // TODO: Use MongoDB aggregation pipeline for true average trust score
        double avg = 0.0;
        return new UserStatsDto(total, active, avg);
    }

    /**
     * Get user by ID.
     * @param userId User ID
     * @return User if found
     * @throws BusinessException if user not found
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
