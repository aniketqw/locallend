package com.locallend.locallend.dto.response;

import com.locallend.locallend.model.RatingType;
import java.time.LocalDateTime;

/**
 * Response DTO for Rating entity.
 * Includes computed fields (timeAgo, canEdit) and optional user/item names.
 * Issue #23: Rating Entity, Repository, DTO
 */
public class RatingResponseDto {
    
    private String id;
    private String raterId;
    private String raterName;
    private String rateeId;
    private String rateeName;
    private String itemId;
    private String itemName;
    private String bookingId;
    private RatingType ratingType;
    private Integer ratingValue;
    private String comment;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Boolean isAnonymous;
    private Integer helpfulCount;
    private Boolean isVerified;
    private String timeAgo;
    private Boolean canEdit;
    
    // Constructors
    public RatingResponseDto() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRaterId() { return raterId; }
    public void setRaterId(String raterId) { this.raterId = raterId; }
    
    public String getRaterName() { return raterName; }
    public void setRaterName(String raterName) { this.raterName = raterName; }
    
    public String getRateeId() { return rateeId; }
    public void setRateeId(String rateeId) { this.rateeId = rateeId; }
    
    public String getRateeName() { return rateeName; }
    public void setRateeName(String rateeName) { this.rateeName = rateeName; }
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public RatingType getRatingType() { return ratingType; }
    public void setRatingType(RatingType ratingType) { this.ratingType = ratingType; }
    
    public Integer getRatingValue() { return ratingValue; }
    public void setRatingValue(Integer ratingValue) { this.ratingValue = ratingValue; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
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
    
    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
}
