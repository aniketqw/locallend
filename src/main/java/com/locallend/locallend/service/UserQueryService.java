package com.locallend.locallend.service;

import com.locallend.locallend.model.User;
import com.locallend.locallend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Read-heavy query service for user search and retrieval operations.
 * Separates read operations from write operations in UserService.
 */
@Service
public class UserQueryService {
    private final UserRepository userRepository;

    @Autowired
    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Find user by username or email.
     * @param usernameOrEmail Username or email to search
     * @return Optional containing user if found
     */
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * List active users with pagination and sorting.
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sort Sort criteria
     * @return Page of active users
     */
    public Page<User> listActiveUsers(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findByIsActiveTrue(pageable);
    }

    /**
     * Get top-rated active users.
     * @param limit Maximum number of users to return
     * @return List of top-rated users
     */
    public List<User> topRatedActiveUsers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "trustScore"));
        Page<User> page = new PageImpl<>(userRepository.findTopRatedActiveUsers(pageable));
        return page.getContent();
    }

    /**
     * Search active users by name or username.
     * @param term Search term (case-insensitive regex)
     * @return List of matching users
     */
    public List<User> searchActiveUsers(String term) {
        return userRepository.searchActiveUsersByNameOrUsername(term);
    }

    /**
     * Search active users by name or username with pagination.
     * @param term Search term (case-insensitive regex)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Page of matching users
     */
    public Page<User> searchActiveUsers(String term, int page, int size) {
        List<User> results = userRepository.searchActiveUsersByNameOrUsername(term);
        Pageable pageable = PageRequest.of(page, size);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        
        List<User> pageContent = results.subList(start, end);
        return new PageImpl<>(pageContent, pageable, results.size());
    }

    /**
     * Find user by ID.
     * @param userId User ID
     * @return Optional containing user if found
     */
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }
}

