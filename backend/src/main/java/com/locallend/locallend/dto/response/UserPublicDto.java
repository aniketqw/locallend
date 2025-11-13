package com.locallend.locallend.dto.response;

/**
 * Public user information DTO.
 * Contains only publicly visible user information (no email, phone, etc.).
 */
public class UserPublicDto {
    private String id;
    private String username;
    private String name;
    private String profileImageUrl;
    private Double trustScore;
    private Long itemCount;
    
    // Computed field for Issue #29
    private String memberSince; // "Member since MMMM yyyy"

    public UserPublicDto() {}

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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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
}
