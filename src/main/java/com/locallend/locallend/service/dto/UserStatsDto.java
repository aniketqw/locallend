package com.locallend.locallend.service.dto;

/**
 * DTO for delivering user statistics.
 * Contains aggregate counts and averages.
 */
public class UserStatsDto {
    private long totalUsers;
    private long activeUsers;
    private double averageTrustScore;

    public UserStatsDto() {}

    public UserStatsDto(long totalUsers, long activeUsers, double averageTrustScore) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.averageTrustScore = averageTrustScore;
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public double getAverageTrustScore() {
        return averageTrustScore;
    }

    public void setAverageTrustScore(double averageTrustScore) {
        this.averageTrustScore = averageTrustScore;
    }
}
