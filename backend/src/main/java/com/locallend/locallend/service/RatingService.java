package com.locallend.locallend.service;

import com.locallend.locallend.model.Rating;
import com.locallend.locallend.model.RatingType;
import com.locallend.locallend.model.User;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.repository.RatingRepository;
import com.locallend.locallend.repository.UserRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.util.TrustScoreCalculator;
import com.locallend.locallend.exception.ResourceNotFoundException;
import com.locallend.locallend.exception.ValidationException;
import com.locallend.locallend.exception.RatingNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for managing ratings in the LocalLend platform.
 * 
 * This service handles all rating-related business logic including:
 * - Creating and updating ratings
 * - Calculating user trust scores
 * - Computing item average ratings
 * - Enforcing business rules and validations
 * - Managing rating lifecycle operations
 * 
 * Key Business Rules:
 * - Users cannot rate their own items
 * - Users can only submit one rating per item
 * - Ratings must be between 1-5 stars
 * - Trust scores are recalculated after each rating change
 * - Ratings can be edited within 7 days of creation
 * 
 * Issue #24: Rating Service Logic
 */
@Service
@Transactional
public class RatingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    private static final int RATING_EDIT_WINDOW_DAYS = 7;
    
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final TrustScoreCalculator trustScoreCalculator;
    
    public RatingService(RatingRepository ratingRepository,
                        UserRepository userRepository,
                        ItemRepository itemRepository,
                        BookingRepository bookingRepository,
                        TrustScoreCalculator trustScoreCalculator) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
        this.trustScoreCalculator = trustScoreCalculator;
    }
    
    /**
     * Create a new item rating.
     * 
     * @param raterId ID of the user giving the rating
     * @param itemId ID of the item being rated
     * @param ratingValue Rating value (1-5)
     * @param comment Optional comment with the rating
     * @param isAnonymous Whether the rating is anonymous
     * @return The created Rating object
     * @throws ValidationException if business rules are violated
     * @throws ResourceNotFoundException if user or item doesn't exist
     */
    public Rating createItemRating(String raterId, String itemId, Integer ratingValue, 
                                  String comment, Boolean isAnonymous) {
        logger.info("Creating item rating: raterId={}, itemId={}, rating={}", raterId, itemId, ratingValue);
        
        // Validate inputs
        validateRatingInput(ratingValue);
        
        // Get user and item entities
        Item item = getItemById(itemId);
        
        // Apply business rules
        if (item.getOwner() != null && item.getOwner().getId().equals(raterId)) {
            throw new ValidationException("Users cannot rate their own items");
        }
        
        // Check for duplicate rating
        Optional<Rating> existingRating = ratingRepository.findByRaterIdAndItemId(raterId, itemId);
        if (existingRating.isPresent()) {
            throw new ValidationException("User has already rated this item. Use updateRating instead.");
        }
        
        // Create rating
        Rating newRating = new Rating();
        newRating.setRaterId(raterId);
        newRating.setRateeId(item.getOwner() != null ? item.getOwner().getId() : null); // Item owner is the ratee
        newRating.setItemId(itemId);
        newRating.setRatingType(RatingType.USER_TO_ITEM);
        newRating.setRatingValue(ratingValue);
        newRating.setComment(sanitizeComment(comment));
        newRating.setIsAnonymous(isAnonymous != null ? isAnonymous : false);
        
        // Save rating
        Rating savedRating = ratingRepository.save(newRating);
        
        // Update derived metrics asynchronously
        updateItemAverageRating(itemId);
        
        logger.info("Successfully created item rating with ID: {}", savedRating.getId());
        return savedRating;
    }
    
    /**
     * Create a user-to-user rating (trust rating).
     * 
     * @param raterId ID of the user giving the rating
     * @param ratedUserId ID of the user being rated
     * @param ratingType Type of user rating (USER_TO_USER, OWNER_TO_BORROWER, BORROWER_TO_OWNER)
     * @param ratingValue Rating value (1-5)
     * @param comment Optional comment with the rating
     * @param bookingId Optional booking ID for verification
     * @return The created Rating object
     */
    public Rating createUserRating(String raterId, String ratedUserId, RatingType ratingType,
                                  Integer ratingValue, String comment, String bookingId) {
        logger.info("Creating user rating: raterId={}, ratedUserId={}, type={}, rating={}", 
                   raterId, ratedUserId, ratingType, ratingValue);
        
        // Validate inputs
        validateRatingInput(ratingValue);
        
        if (raterId.equals(ratedUserId)) {
            throw new ValidationException("Users cannot rate themselves");
        }
        
        if (!ratingType.affectsTrustScore()) {
            throw new ValidationException("Invalid rating type for user rating. Use USER_TO_USER, OWNER_TO_BORROWER, or BORROWER_TO_OWNER");
        }
        // Validate booking if provided (Issue #26)
        validateBookingForRating(bookingId, raterId);

        
        // Get user entities (verify they exist)
        getUserById(raterId);
        getUserById(ratedUserId);
        
        // Check for duplicate rating for this type
        Optional<Rating> existingRating = ratingRepository.findByRaterIdAndRateeIdAndRatingType(
            raterId, ratedUserId, ratingType);
        if (existingRating.isPresent()) {
            throw new ValidationException("User has already submitted this type of rating for this user. Use updateRating instead.");
        }
        
        // Create rating
        Rating newRating = new Rating();
        newRating.setRaterId(raterId);
        newRating.setRateeId(ratedUserId);
        newRating.setRatingType(ratingType);
        newRating.setRatingValue(ratingValue);
        newRating.setComment(sanitizeComment(comment));
        newRating.setBookingId(bookingId); // This also sets isVerified if bookingId present
        
        // Save rating
        Rating savedRating = ratingRepository.save(newRating);
        
        // Update user trust score
        updateUserTrustScore(ratedUserId);
        
        // Mark booking as rated (Issue #26)
        markBookingAsRated(bookingId);
        
        logger.info("Successfully created user rating with ID: {}", savedRating.getId());
        return savedRating;
    }
    
    /**
     * Update an existing rating.
     * 
     * @param ratingId ID of the rating to update
     * @param raterId ID of the user updating the rating (for authorization)
     * @param newRatingValue New rating value (1-5)
     * @param newComment New comment
     * @return The updated Rating object
     */
    public Rating updateRating(String ratingId, String raterId, Integer newRatingValue, String newComment) {
        logger.info("Updating rating: ratingId={}, raterId={}, newRating={}", 
                   ratingId, raterId, newRatingValue);
        
        // Validate inputs
        validateRatingInput(newRatingValue);
        
        // Get existing rating
        Rating existingRating = getRatingById(ratingId);
        
        // Check authorization - only the original rater can update
        if (!existingRating.getRaterId().equals(raterId)) {
            throw new ValidationException("Only the original rater can update this rating");
        }
        
        // Check if rating is still editable (within time window)
        if (isRatingTooOldToEdit(existingRating)) {
            throw new ValidationException(String.format(
                "Rating is too old to be edited. Ratings can only be edited within %d days of creation.",
                RATING_EDIT_WINDOW_DAYS));
        }
        
        // Update rating
        existingRating.setRatingValue(newRatingValue);
        existingRating.setComment(sanitizeComment(newComment));
        
        // Save updated rating
        Rating updatedRating = ratingRepository.save(existingRating);
        
        // Update derived metrics
        if (existingRating.getRatingType().affectsItemQuality()) {
            updateItemAverageRating(existingRating.getItemId());
        }
        if (existingRating.getRatingType().affectsTrustScore()) {
            updateUserTrustScore(existingRating.getRateeId());
        }
        
        logger.info("Successfully updated rating with ID: {}", updatedRating.getId());
        return updatedRating;
    }
    
    /**
     * Get all ratings for a specific item.
     * 
     * @param itemId ID of the item
     * @return List of ratings for the item
     */
    @Transactional(readOnly = true)
    public List<Rating> getItemRatings(String itemId) {
        logger.debug("Fetching ratings for item: {}", itemId);
        
        // Verify item exists
        getItemById(itemId);
        
        return ratingRepository.findByItemIdOrderByCreatedDateDesc(itemId);
    }
    
    /**
     * Get all ratings given by a specific user.
     * 
     * @param raterId ID of the rater
     * @return List of ratings given by the user
     */
    @Transactional(readOnly = true)
    public List<Rating> getRatingsByRater(String raterId) {
        logger.debug("Fetching ratings by rater: {}", raterId);
        
        // Verify user exists
        getUserById(raterId);
        
        return ratingRepository.findByRaterIdOrderByCreatedDateDesc(raterId);
    }
    
    /**
     * Get all ratings received by a specific user.
     * 
     * @param userId ID of the user
     * @return List of ratings received by the user
     */
    @Transactional(readOnly = true)
    public List<Rating> getRatingsReceivedByUser(String userId) {
        logger.debug("Fetching ratings received by user: {}", userId);
        
        // Verify user exists
        getUserById(userId);
        
        return ratingRepository.findByRateeIdOrderByCreatedDateDesc(userId);
    }
    
    /**
     * Get average rating for a specific item.
     * 
     * @param itemId ID of the item
     * @return Average rating (0.0 if no ratings)
     */
    @Transactional(readOnly = true)
    public Double getItemAverageRating(String itemId) {
        logger.debug("Calculating average rating for item: {}", itemId);
        
        List<Rating> ratings = ratingRepository.findByItemId(itemId);
        
        if (ratings.isEmpty()) {
            return 0.0;
        }
        
        double average = ratings.stream()
                .mapToDouble(Rating::getRatingValue)
                .average()
                .orElse(0.0);
        
        return Math.round(average * 10.0) / 10.0; // Round to 1 decimal place
    }
    
    /**
     * Get trust score for a specific user.
     * 
     * @param userId ID of the user
     * @return Trust score calculated using TrustScoreCalculator
     */
    @Transactional(readOnly = true)
    public Double getUserTrustScore(String userId) {
        logger.debug("Calculating trust score for user: {}", userId);
        
        // Verify user exists
        getUserById(userId);
        
        // Get all user ratings (not item ratings)
        List<Rating> userRatings = ratingRepository.findUserRatingsForTrustScore(userId);
        
        return trustScoreCalculator.calculateTrustScore(userRatings);
    }
    
    /**
     * Get rating statistics for an item.
     * 
     * @param itemId ID of the item
     * @return RatingStatistics object with detailed rating breakdown
     */
    @Transactional(readOnly = true)
    public RatingStatistics getItemRatingStatistics(String itemId) {
        logger.debug("Fetching rating statistics for item: {}", itemId);
        
        List<Rating> ratings = ratingRepository.findByItemId(itemId);
        
        return calculateRatingStatistics(ratings);
    }
    
    /**
     * Get rating statistics for a user.
     * 
     * @param userId ID of the user
     * @return RatingStatistics object with detailed rating breakdown
     */
    @Transactional(readOnly = true)
    public RatingStatistics getUserRatingStatistics(String userId) {
        logger.debug("Fetching rating statistics for user: {}", userId);
        
        List<Rating> ratings = ratingRepository.findByRateeIdOrderByCreatedDateDesc(userId);
        
        return calculateRatingStatistics(ratings);
    }
    
    /**
     * Get a specific rating by ID.
     * 
     * @param ratingId ID of the rating
     * @return Rating object
     */
    @Transactional(readOnly = true)
    public Rating getRatingById(String ratingId) {
        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException(ratingId));
    }
    
    /**
     * Delete a rating.
     * Only the original rater can delete their rating.
     * 
     * @param ratingId ID of the rating to delete
     * @param userId ID of the user requesting deletion (for authorization)
     */
    public void deleteRating(String ratingId, String userId) {
        logger.info("Deleting rating: ratingId={}, userId={}", ratingId, userId);
        
        Rating rating = getRatingById(ratingId);
        
        // Check authorization - only the rater can delete
        if (!rating.getRaterId().equals(userId)) {
            throw new ValidationException("Only the original rater can delete this rating");
        }
        
        // Delete rating
        ratingRepository.delete(rating);
        
        // Update derived metrics
        if (rating.getRatingType().affectsItemQuality() && rating.getItemId() != null) {
            updateItemAverageRating(rating.getItemId());
        }
        if (rating.getRatingType().affectsTrustScore()) {
            updateUserTrustScore(rating.getRateeId());
        }
        
        logger.info("Successfully deleted rating with ID: {}", ratingId);
    }
    
    // Private helper methods
    
    private void validateRatingInput(Integer rating) {
        if (rating == null) {
            throw new ValidationException("Rating cannot be null");
        }
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
    }
    
    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
    
    private Item getItemById(String itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemId));
    }
    
    private String sanitizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        
        // Trim whitespace
        comment = comment.trim();
        
        // Return null if empty
        if (comment.isEmpty()) {
            return null;
        }
        
        // Limit length (enforced by validation, but double-check)
        if (comment.length() > 1000) {
            comment = comment.substring(0, 997) + "...";
        }
        
        // Basic sanitization - remove potentially dangerous characters
        comment = comment.replaceAll("[<>\"']", "");
        
        return comment;
    }
    
    private boolean isRatingTooOldToEdit(Rating rating) {
        // Allow editing within RATING_EDIT_WINDOW_DAYS of creation
        LocalDateTime cutoff = rating.getCreatedDate().plusDays(RATING_EDIT_WINDOW_DAYS);
        return LocalDateTime.now().isAfter(cutoff);
    }
    
    private void updateUserTrustScore(String userId) {
        try {
            Double trustScore = getUserTrustScore(userId);
            
            // Update user entity with new trust score
            User user = getUserById(userId);
            user.setTrustScore(trustScore);
            userRepository.save(user);
            
            logger.debug("Updated trust score for user {}: {}", userId, trustScore);
        } catch (Exception e) {
            logger.error("Error updating trust score for user {}: {}", userId, e.getMessage());
        }
    }
    
    private void updateItemAverageRating(String itemId) {
        try {
            Double averageRating = getItemAverageRating(itemId);
            
            // Update item entity with new average rating if it has that field
            // Note: Our Item entity may not have averageRating field yet
            // This is a placeholder for future enhancement
            logger.debug("Calculated average rating for item {}: {}", itemId, averageRating);
        } catch (Exception e) {
            logger.error("Error calculating average rating for item {}: {}", itemId, e.getMessage());
        }
    }
    
    private RatingStatistics calculateRatingStatistics(List<Rating> ratings) {
        if (ratings.isEmpty()) {
            return new RatingStatistics(0, 0.0, new int[]{0, 0, 0, 0, 0});
        }
        
        int totalRatings = ratings.size();
        double averageRating = ratings.stream()
                .mapToDouble(Rating::getRatingValue)
                .average()
                .orElse(0.0);
        
        int[] ratingDistribution = new int[5]; // Index 0 = 1-star, Index 4 = 5-star
        
        ratings.forEach(rating -> {
            int ratingValue = rating.getRatingValue();
            if (ratingValue >= 1 && ratingValue <= 5) {
                ratingDistribution[ratingValue - 1]++;
            }
        });
        
        return new RatingStatistics(totalRatings, averageRating, ratingDistribution);
    }
    
    /**
     * Inner class for rating statistics.
     */
    public static class RatingStatistics {
        private final int totalRatings;
        private final double averageRating;
        private final int[] ratingDistribution; // [1-star count, 2-star count, ..., 5-star count]
        
        public RatingStatistics(int totalRatings, double averageRating, int[] ratingDistribution) {
            this.totalRatings = totalRatings;
            this.averageRating = Math.round(averageRating * 10.0) / 10.0;
            this.ratingDistribution = ratingDistribution.clone();
        }
        
        // Getters
        public int getTotalRatings() { return totalRatings; }
        public double getAverageRating() { return averageRating; }
        public int[] getRatingDistribution() { return ratingDistribution.clone(); }
        public int getOneStarCount() { return ratingDistribution[0]; }
        public int getTwoStarCount() { return ratingDistribution[1]; }
        public int getThreeStarCount() { return ratingDistribution[2]; }
        public int getFourStarCount() { return ratingDistribution[3]; }
        public int getFiveStarCount() { return ratingDistribution[4]; }
        
        public double getPercentageForRating(int rating) {
            if (rating < 1 || rating > 5 || totalRatings == 0) {
                return 0.0;
            }
            return Math.round((double) ratingDistribution[rating - 1] / totalRatings * 1000.0) / 10.0;
        }
    }

    // ===== Issue #26: Booking-Rating Integration Methods =====
    
    private void validateBookingForRating(String bookingId, String raterId) {
        if (bookingId == null || bookingId.trim().isEmpty()) return;
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ValidationException("Cannot rate - booking not completed. Status: " + booking.getStatus());
        }
        if (!booking.getBorrowerId().equals(raterId)) {
            throw new ValidationException("Only the borrower can rate this booking");
        }
        if (Boolean.TRUE.equals(booking.getIsRated())) {
            throw new ValidationException("This booking has already been rated");
        }
    }
    
    @Transactional(readOnly = true)
    public boolean canRateBooking(String bookingId, String userId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            return booking != null && booking.getStatus() == BookingStatus.COMPLETED 
                   && booking.getBorrowerId().equals(userId) 
                   && !Boolean.TRUE.equals(booking.getIsRated());
        } catch (Exception e) {
            return false;
        }
    }
    
    private void markBookingAsRated(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) return;
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null) {
                booking.setIsRated(true);
                bookingRepository.save(booking);
            }
        } catch (Exception e) {
            logger.error("Error marking booking as rated: {}", e.getMessage());
        }
    }
}