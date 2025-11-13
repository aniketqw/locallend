package com.locallend.locallend.dto.request;

import com.locallend.locallend.model.RatingType;
import jakarta.validation.constraints.*;

/**
 * DTO for creating a new rating.
 * Includes validation for rating values and cross-field validation for item ratings.
 * Issue #23: Rating Entity, Repository, DTO
 */
public class RatingCreateDto {
    
    @NotBlank(message = "Ratee ID is required")
    private String rateeId;
    
    private String itemId;
    private String bookingId;
    
    @NotNull(message = "Rating type is required")
    private RatingType ratingType;
    
    @NotNull(message = "Rating value is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer ratingValue;
    
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
    
    private Boolean isAnonymous = false;
    
    // Constructors
    public RatingCreateDto() {}
    
    public RatingCreateDto(String rateeId, RatingType ratingType, Integer ratingValue) {
        this.rateeId = rateeId;
        this.ratingType = ratingType;
        this.ratingValue = ratingValue;
    }
    
    // Cross-field validation
    @AssertTrue(message = "Item ID is required for item ratings")
    public boolean isItemIdValidForType() {
        if (ratingType == RatingType.USER_TO_ITEM) {
            return itemId != null && !itemId.trim().isEmpty();
        }
        return true;
    }
    
    // Getters and Setters
    public String getRateeId() { return rateeId; }
    public void setRateeId(String rateeId) { this.rateeId = rateeId; }
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public RatingType getRatingType() { return ratingType; }
    public void setRatingType(RatingType ratingType) { this.ratingType = ratingType; }
    
    public Integer getRatingValue() { return ratingValue; }
    public void setRatingValue(Integer ratingValue) { this.ratingValue = ratingValue; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
}
