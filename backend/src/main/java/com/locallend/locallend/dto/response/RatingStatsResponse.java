package com.locallend.locallend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Response DTO for rating statistics.
 * Provides aggregated rating data for users or items.
 * Issue #25: Rating Controller APIs
 */
public class RatingStatsResponse {

    @JsonProperty("subject_id")
    private String subjectId; // User ID or Item ID

    @JsonProperty("subject_type")
    private String subjectType; // "USER" or "ITEM"

    @JsonProperty("average_rating")
    private Double averageRating;

    @JsonProperty("total_ratings")
    private Long totalRatings;

    @JsonProperty("rating_distribution")
    private Map<String, Long> ratingDistribution; // "5" -> count, "4" -> count, etc.

    @JsonProperty("recent_average")
    private Double recentAverage; // Last 30 days

    @JsonProperty("verified_ratings_count")
    private Long verifiedRatingsCount;

    // Constructors
    public RatingStatsResponse() {}

    public RatingStatsResponse(String subjectId, String subjectType, Double averageRating, Long totalRatings) {
        this.subjectId = subjectId;
        this.subjectType = subjectType;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }

    // Getters and Setters
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

    public Map<String, Long> getRatingDistribution() { return ratingDistribution; }
    public void setRatingDistribution(Map<String, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }

    public Double getRecentAverage() { return recentAverage; }
    public void setRecentAverage(Double recentAverage) { this.recentAverage = recentAverage; }

    public Long getVerifiedRatingsCount() { return verifiedRatingsCount; }
    public void setVerifiedRatingsCount(Long verifiedRatingsCount) { this.verifiedRatingsCount = verifiedRatingsCount; }

    // Helper methods
    public String getFormattedAverageRating() {
        return averageRating != null ? String.format("%.1f", averageRating) : "N/A";
    }

    public boolean hasRatings() {
        return totalRatings != null && totalRatings > 0;
    }
}
