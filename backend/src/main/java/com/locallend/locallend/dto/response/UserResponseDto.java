package com.locallend.locallend.dto.response;

import java.time.LocalDateTime;

/**
 * Full user response DTO for authenticated user operations.
 * Contains all non-sensitive user information.
 */
public class UserResponseDto {
    private String id;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private Double trustScore;
    private Long itemCount;
    
    // Computed fields for Issue #29
    private String memberSince; // "Member since MMMM yyyy"
    private Integer totalItemsShared; // Total items created by user
    private Integer totalBookings; // Total bookings as owner or borrower

    public UserResponseDto() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(Double trustScore) {
        this.trustScore = trustScore;
    }

    public Long getItemCount() {
        return itemCount;
    }

    public void setItemCount(Long itemCount) {
        this.itemCount = itemCount;
    }
    
    public String getMemberSince() {
        return memberSince;
    }
    
    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }
    
    public Integer getTotalItemsShared() {
        return totalItemsShared;
    }
    
    public void setTotalItemsShared(Integer totalItemsShared) {
        this.totalItemsShared = totalItemsShared;
    }
    
    public Integer getTotalBookings() {
        return totalBookings;
    }
    
    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }
}
