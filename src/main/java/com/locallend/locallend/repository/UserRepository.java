package com.locallend.locallend.repository;
import com.locallend.locallend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

/** 
repository for the User entity . Provide CRUD operation ;
**/

public interface UserRepository extends MongoRepository<User, String>{
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find user by username OR email
    @Query("{'$or': [{'username': ?0}, {'email': ?0}]}")
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    
    // Count active users
    Long countByIsActiveTrue();
    
    // Find active users with pagination
    Page<User> findByIsActiveTrue(Pageable pageable);
    
    // Find top-rated active users
    @Query("{'isActive': true}")
    List<User> findTopRatedActiveUsers(Pageable pageable);
    
    // Search active users by name or username
    @Query("{'isActive': true, '$or': [{'name': {'$regex': ?0, '$options': 'i'}}, {'username': {'$regex': ?0, '$options': 'i'}}]}")
    List<User> searchActiveUsersByNameOrUsername(String searchTerm);
    
    // Issue #29: Geospatial queries for proximity-based features
    
    // Find active users near a location within a given distance
    List<User> findByLocationNearAndIsActiveTrue(Point location, Distance distance);
    
    // Find active users with minimum trust score
    @Query("{'isActive': true, 'trust_score': {'$gte': ?0}}")
    List<User> findActiveUsersWithMinTrustScore(Double minTrustScore);
    
    // Issue #25: Update user trust score
    // This is a custom method that will be implemented in RatingService
    // using direct MongoDB update operations or by loading and saving the user
    default void updateTrustScore(String userId, Double trustScore) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTrustScore(trustScore);
            user.setLastTrustScoreUpdate(java.time.LocalDateTime.now());
            save(user);
        }
    }
}

