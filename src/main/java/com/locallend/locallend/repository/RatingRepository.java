package com.locallend.locallend.repository;

import com.locallend.locallend.model.Rating;
import com.locallend.locallend.model.RatingType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Rating entity.
 * Provides queries for user ratings, item ratings, and statistical aggregations.
 * Issue #23: Rating Entity, Repository, DTO
 */
@Repository
public interface RatingRepository extends MongoRepository<Rating, String> {
    
    // Basic Finders
    List<Rating> findByRaterIdOrderByCreatedDateDesc(String raterId);
    List<Rating> findByRateeIdOrderByCreatedDateDesc(String rateeId);
    List<Rating> findByBookingId(String bookingId);
    Optional<Rating> findByRaterIdAndBookingId(String raterId, String bookingId);
    Optional<Rating> findByRaterIdAndItemId(String raterId, String itemId);
    Optional<Rating> findByRaterIdAndRateeIdAndRatingType(String raterId, String rateeId, RatingType ratingType);
    List<Rating> findByItemId(String itemId);
    
    // Rating Type Specific
    List<Rating> findByRateeIdAndRatingTypeOrderByCreatedDateDesc(String rateeId, RatingType ratingType);
    List<Rating> findByItemIdOrderByCreatedDateDesc(String itemId);
    
    // User Rating Queries (for trust score)
    @Query("{'rateeId': ?0, 'ratingType': {'$in': ['USER_TO_USER', 'OWNER_TO_BORROWER', 'BORROWER_TO_OWNER']}}")
    List<Rating> findUserRatingsForTrustScore(String userId);
    
    @Query("{'rateeId': ?0, 'ratingType': {'$in': ['USER_TO_USER', 'OWNER_TO_BORROWER', 'BORROWER_TO_OWNER']}, " +
           "'createdDate': {'$gte': ?1}}")
    List<Rating> findRecentUserRatings(String userId, LocalDateTime since);
    
    @Query("{'rateeId': ?0, 'ratingType': {'$in': ['USER_TO_USER', 'OWNER_TO_BORROWER', 'BORROWER_TO_OWNER']}, " +
           "'isVerified': true}")
    List<Rating> findVerifiedUserRatings(String userId);
    
    // Item Rating Queries
    @Query("{'itemId': ?0, 'ratingType': 'USER_TO_ITEM', 'isVerified': true}")
    List<Rating> findVerifiedItemRatings(String itemId);
    
    // Statistical Queries
    long countByRateeIdAndRatingType(String rateeId, RatingType ratingType);
    long countByRaterId(String raterId);
    
    // Average Rating Calculation using MongoDB Aggregation
    @Aggregation(pipeline = {
        "{ '$match': { 'rateeId': ?0, 'ratingType': { '$in': ['USER_TO_USER', 'OWNER_TO_BORROWER', 'BORROWER_TO_OWNER'] } } }",
        "{ '$group': { '_id': '$rateeId', 'averageRating': { '$avg': '$ratingValue' }, 'totalRatings': { '$sum': 1 } } }"
    })
    Optional<UserRatingAggregate> calculateUserAverageRating(String userId);
    
    @Aggregation(pipeline = {
        "{ '$match': { 'itemId': ?0, 'ratingType': 'USER_TO_ITEM' } }",
        "{ '$group': { '_id': '$itemId', 'averageRating': { '$avg': '$ratingValue' }, 'totalRatings': { '$sum': 1 } } }"
    })
    Optional<ItemRatingAggregate> calculateItemAverageRating(String itemId);
    
    // Aggregation Result Interfaces
    interface UserRatingAggregate {
        String getId();
        Double getAverageRating();
        Long getTotalRatings();
    }
    
    interface ItemRatingAggregate {
        String getId();
        Double getAverageRating();
        Long getTotalRatings();
    }
}
