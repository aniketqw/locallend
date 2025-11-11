package com.locallend.locallend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Rating entity for LocalLend system.
 * Supports bidirectional ratings (user-to-user and user-to-item) with booking integration.
 * Issue #23: Rating Entity, Repository, DTO
 */
@Document(collection = "ratings")
@CompoundIndexes({
    @CompoundIndex(name = "rater_ratee_idx", def = "{'raterId': 1, 'rateeId': 1, 'ratingType': 1}"),
    @CompoundIndex(name = "booking_idx", def = "{'bookingId': 1}"),
    @CompoundIndex(name = "item_rating_idx", def = "{'itemId': 1, 'ratingValue': -1}")
})
public class Rating {
    
    @Id
    private String id;
    
    @Indexed
    @NotBlank(message = "Rater ID is required")
    @Field("rater_id")
    private String raterId;
    
    @Indexed
    @NotBlank(message = "Ratee ID is required")
    @Field("ratee_id")
    private String rateeId;
    
    @Field("item_id")
    private String itemId;
    
    @Field("booking_id")
    private String bookingId;
    
    @NotNull(message = "Rating type is required")
    @Field("rating_type")
    private RatingType ratingType;
    
    @NotNull(message = "Rating value is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Field("rating_value")
    private Integer ratingValue;
    
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
    
    @Field("created_date")
    private LocalDateTime createdDate;
    
    @Field("updated_date")
    private LocalDateTime updatedDate;
    
    @Field("is_anonymous")
    private Boolean isAnonymous = false;
    
    @Field("helpful_count")
    private Integer helpfulCount = 0;
    
    @Field("is_verified")
    private Boolean isVerified = false;
    
    // Constructors
    public Rating() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public Rating(String raterId, String rateeId, RatingType ratingType, Integer ratingValue) {
        this();
        this.raterId = raterId;
        this.rateeId = rateeId;
        this.ratingType = ratingType;
        this.ratingValue = ratingValue;
    }
    
    // Business Logic Methods
    
    /**
     * Check if this rating affects user trust score.
     * @return true if rating should be included in trust score calculation
     */
    public boolean isUserRating() {
        return ratingType.affectsTrustScore();
    }
    
    /**
     * Check if this rating affects item quality score.
     * @return true if rating should be included in item quality calculation
     */
    public boolean isItemRating() {
        return ratingType.affectsItemQuality();
    }
    
    // Getters and Setters
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRaterId() { return raterId; }
    public void setRaterId(String raterId) { this.raterId = raterId; }
    
    public String getRateeId() { return rateeId; }
    public void setRateeId(String rateeId) { this.rateeId = rateeId; }
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { 
        this.bookingId = bookingId;
        // Automatically verify rating if linked to booking
        this.isVerified = (bookingId != null);
    }
    
    public RatingType getRatingType() { return ratingType; }
    public void setRatingType(RatingType ratingType) { this.ratingType = ratingType; }
    
    public Integer getRatingValue() { return ratingValue; }
    public void setRatingValue(Integer ratingValue) { 
        this.ratingValue = ratingValue;
        this.updatedDate = LocalDateTime.now();
    }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { 
        this.comment = comment;
        this.updatedDate = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
    
    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
}
