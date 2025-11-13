package com.locallend.locallend.util;

import com.locallend.locallend.model.Rating;
import com.locallend.locallend.dto.request.RatingCreateDto;
import com.locallend.locallend.dto.response.RatingResponseDto;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting Rating entities to/from DTOs.
 * Includes time-based calculations for "time ago" display.
 * Issue #23: Rating Entity, Repository, DTO
 */
@Component
public class RatingMapper {
    
    /**
     * Convert RatingCreateDto to Rating entity.
     * @param dto Rating creation request DTO
     * @param raterId ID of the user creating the rating
     * @return Rating entity
     */
    public Rating toEntity(RatingCreateDto dto, String raterId) {
        if (dto == null) return null;
        
        Rating rating = new Rating();
        rating.setRaterId(raterId);
        rating.setRateeId(dto.getRateeId());
        rating.setItemId(dto.getItemId());
        rating.setBookingId(dto.getBookingId());
        rating.setRatingType(dto.getRatingType());
        rating.setRatingValue(dto.getRatingValue());
        rating.setComment(dto.getComment());
        rating.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        rating.setIsVerified(dto.getBookingId() != null);
        
        return rating;
    }
    
    /**
     * Convert Rating entity to RatingResponseDto.
     * @param rating Rating entity
     * @return RatingResponseDto with computed fields
     */
    public RatingResponseDto toResponseDto(Rating rating) {
        if (rating == null) return null;
        
        RatingResponseDto dto = new RatingResponseDto();
        dto.setId(rating.getId());
        dto.setRaterId(rating.getRaterId());
        dto.setRateeId(rating.getRateeId());
        dto.setItemId(rating.getItemId());
        dto.setBookingId(rating.getBookingId());
        dto.setRatingType(rating.getRatingType());
        dto.setRatingValue(rating.getRatingValue());
        dto.setComment(rating.getComment());
        dto.setCreatedDate(rating.getCreatedDate());
        dto.setUpdatedDate(rating.getUpdatedDate());
        dto.setIsAnonymous(rating.getIsAnonymous());
        dto.setHelpfulCount(rating.getHelpfulCount());
        dto.setIsVerified(rating.getIsVerified());
        dto.setTimeAgo(calculateTimeAgo(rating.getCreatedDate()));
        
        return dto;
    }
    
    /**
     * Convert list of Rating entities to list of RatingResponseDtos.
     * @param ratings List of Rating entities
     * @return List of RatingResponseDto
     */
    public List<RatingResponseDto> toResponseDtoList(List<Rating> ratings) {
        if (ratings == null) return null;
        return ratings.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate human-readable "time ago" string from LocalDateTime.
     * @param dateTime Past date/time
     * @return String like "5 minutes ago", "2 hours ago", "3 days ago"
     */
    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        if (hours < 24) return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        return days + " day" + (days != 1 ? "s" : "") + " ago";
    }
}
