package com.locallend.locallend.util;

import com.locallend.locallend.dto.response.UserPublicDto;
import com.locallend.locallend.dto.response.UserResponseDto;
import com.locallend.locallend.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper utility for converting User entities to DTOs.
 * Ensures sensitive data is not exposed in public responses.
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to full UserResponseDto (for authenticated user).
     * @param user User entity
     * @return UserResponseDto with all non-sensitive fields
     */
    public UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setTrustScore(user.getTrustScore());
        dto.setItemCount(user.getItemCount());
        
        // Computed fields for Issue #29
        dto.setMemberSince(formatMemberSince(user.getCreatedDate()));
        // Note: totalItemsShared and totalBookings require item/booking repository queries
        // These will be populated when ItemRepository and BookingRepository are integrated
        dto.setTotalItemsShared(0); // Placeholder
        dto.setTotalBookings(0);    // Placeholder

        return dto;
    }

    /**
     * Convert User entity to UserPublicDto (for public viewing).
     * Excludes sensitive information like email and phone.
     * @param user User entity
     * @return UserPublicDto with only public fields
     */
    public UserPublicDto toUserPublicDto(User user) {
        if (user == null) {
            return null;
        }

        UserPublicDto dto = new UserPublicDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setTrustScore(user.getTrustScore());
        dto.setItemCount(user.getItemCount());
        
        // Computed field for Issue #29
        dto.setMemberSince(formatMemberSince(user.getCreatedDate()));

        return dto;
    }
    
    /**
     * Format createdDate to "Member since MMMM yyyy" string.
     * @param createdDate User's created date
     * @return Formatted string like "Member since January 2024"
     */
    private String formatMemberSince(LocalDateTime createdDate) {
        if (createdDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return "Member since " + createdDate.format(formatter);
    }
}
