package com.locallend.locallend.util;

import com.locallend.locallend.model.Rating;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.DoubleSummaryStatistics;

/**
 * Utility class for calculating user trust scores in the LocalLend platform.
 * 
 * Trust scores are calculated based on multiple factors:
 * - Average rating received
 * - Number of ratings (volume factor)
 * - Recency of ratings (temporal factor)
 * - Consistency of ratings (variance factor)
 * - Rating type mix (verified vs unverified)
 * 
 * The trust score is normalized to a 0-5 scale matching the user trustScore field.
 * Issue #24: Rating Service Logic
 */
@Component
public class TrustScoreCalculator {
    
    // Algorithm constants - can be tuned based on community feedback
    private static final double BASE_WEIGHT = 0.4;           // Base average rating weight
    private static final double VOLUME_WEIGHT = 0.2;        // Number of ratings weight
    private static final double RECENCY_WEIGHT = 0.2;       // Recent ratings weight
    private static final double CONSISTENCY_WEIGHT = 0.1;   // Rating consistency weight
    private static final double VERIFICATION_WEIGHT = 0.1;  // Verification status weight
    
    private static final int MIN_RATINGS_FOR_FULL_TRUST = 10; // Minimum ratings for maximum volume score
    private static final int DAYS_FOR_RECENT_BONUS = 30;      // Days to consider for recency bonus
    private static final double EXCELLENT_THRESHOLD = 4.5;    // Threshold for excellent ratings
    
    /**
     * Calculate trust score for a user based on their ratings.
     * Score is on a 0-5 scale to match the User entity trustScore field.
     * 
     * @param ratings List of ratings received by the user
     * @return Trust score between 0.0 and 5.0
     */
    public Double calculateTrustScore(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return 5.0; // New users start with neutral 5.0 trust score (Issue #29 default)
        }
        
        // Calculate individual components
        double baseScore = calculateBaseScore(ratings);
        double volumeScore = calculateVolumeScore(ratings);
        double recencyScore = calculateRecencyScore(ratings);
        double consistencyScore = calculateConsistencyScore(ratings);
        double verificationScore = calculateVerificationScore(ratings);
        
        // Combine scores with weights
        double trustScore = (baseScore * BASE_WEIGHT) +
                          (volumeScore * VOLUME_WEIGHT) +
                          (recencyScore * RECENCY_WEIGHT) +
                          (consistencyScore * CONSISTENCY_WEIGHT) +
                          (verificationScore * VERIFICATION_WEIGHT);
        
        // Apply any bonuses or penalties
        trustScore = applyBonusesAndPenalties(trustScore, ratings);
        
        // Normalize to 0-5 scale and round to 1 decimal place
        trustScore = Math.max(0.0, Math.min(5.0, trustScore));
        return Math.round(trustScore * 10.0) / 10.0;
    }
    
    /**
     * Calculate base score from average rating.
     * 
     * @param ratings List of active ratings
     * @return Base score (0.0 to 5.0)
     */
    private double calculateBaseScore(List<Rating> ratings) {
        DoubleSummaryStatistics stats = ratings.stream()
                .mapToDouble(Rating::getRatingValue)
                .summaryStatistics();
        
        return stats.getAverage();
    }
    
    /**
     * Calculate volume score based on number of ratings.
     * More ratings generally indicate higher trustworthiness.
     * 
     * @param ratings List of active ratings
     * @return Volume score (0.0 to 5.0)
     */
    private double calculateVolumeScore(List<Rating> ratings) {
        int ratingCount = ratings.size();
        
        if (ratingCount == 0) {
            return 5.0; // Neutral for no ratings
        }
        
        // Logarithmic scale - diminishing returns after MIN_RATINGS_FOR_FULL_TRUST
        double score = Math.log(ratingCount + 1) / Math.log(MIN_RATINGS_FOR_FULL_TRUST + 1);
        score = Math.min(1.0, score);
        
        // Scale to 0-5, but start from 4.0 to avoid penalizing too heavily
        return 4.0 + score;
    }
    
    /**
     * Calculate recency score - recent ratings are weighted higher.
     * 
     * @param ratings List of active ratings
     * @return Recency score (0.0 to 5.0)
     */
    private double calculateRecencyScore(List<Rating> ratings) {
        LocalDateTime now = LocalDateTime.now();
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (Rating rating : ratings) {
            long daysAgo = ChronoUnit.DAYS.between(rating.getCreatedDate(), now);
            
            // Calculate recency weight (higher for more recent ratings)
            double recencyWeight = Math.exp(-daysAgo / (double) DAYS_FOR_RECENT_BONUS);
            
            weightedSum += rating.getRatingValue() * recencyWeight;
            totalWeight += recencyWeight;
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : 5.0;
    }
    
    /**
     * Calculate consistency score - consistent ratings are better than volatile ones.
     * Lower variance in ratings indicates more consistent service.
     * 
     * @param ratings List of active ratings
     * @return Consistency score (0.0 to 5.0)
     */
    private double calculateConsistencyScore(List<Rating> ratings) {
        if (ratings.size() < 2) {
            return 5.0; // Single rating is perfectly consistent
        }
        
        DoubleSummaryStatistics stats = ratings.stream()
                .mapToDouble(Rating::getRatingValue)
                .summaryStatistics();
        
        double average = stats.getAverage();
        
        // Calculate standard deviation
        double variance = ratings.stream()
                .mapToDouble(r -> Math.pow(r.getRatingValue() - average, 2))
                .sum() / ratings.size();
        
        double stdDev = Math.sqrt(variance);
        
        // Convert standard deviation to consistency score
        // Lower stdDev = higher consistency score
        // Maximum possible stdDev is 2.0 (for 1-5 scale)
        double consistencyRatio = 1.0 - (stdDev / 2.0);
        
        // Scale to average rating range to maintain score around average
        return Math.max(0.0, average * consistencyRatio);
    }
    
    /**
     * Calculate verification score based on verified ratings ratio.
     * Verified ratings (from completed bookings) are more trustworthy.
     * 
     * @param ratings List of active ratings
     * @return Verification score (0.0 to 5.0)
     */
    private double calculateVerificationScore(List<Rating> ratings) {
        long verifiedRatings = ratings.stream()
                .filter(Rating::getIsVerified)
                .count();
        
        if (ratings.isEmpty()) {
            return 5.0;
        }
        
        double verificationRatio = (double) verifiedRatings / ratings.size();
        
        // Calculate average of verified ratings
        DoubleSummaryStatistics verifiedStats = ratings.stream()
                .filter(Rating::getIsVerified)
                .mapToDouble(Rating::getRatingValue)
                .summaryStatistics();
        
        double verifiedAverage = verifiedStats.getCount() > 0 ? verifiedStats.getAverage() : 5.0;
        
        // Weight verified ratings more heavily
        return verifiedAverage * (0.7 + 0.3 * verificationRatio);
    }
    
    /**
     * Apply bonuses and penalties based on specific criteria.
     * 
     * @param baseScore Current trust score
     * @param ratings List of active ratings
     * @return Adjusted trust score
     */
    private double applyBonusesAndPenalties(double baseScore, List<Rating> ratings) {
        double adjustedScore = baseScore;
        
        // Excellence bonus: if user has high percentage of excellent ratings (4.5+)
        long excellentRatings = ratings.stream()
                .filter(r -> r.getRatingValue() >= EXCELLENT_THRESHOLD)
                .count();
        
        if (excellentRatings > 0) {
            double excellenceRatio = (double) excellentRatings / ratings.size();
            if (excellenceRatio >= 0.8) {
                adjustedScore += 0.3; // Bonus for 80%+ excellent ratings
            } else if (excellenceRatio >= 0.6) {
                adjustedScore += 0.2; // Bonus for 60%+ excellent ratings
            }
        }
        
        // Penalty for having very low ratings
        long lowRatings = ratings.stream()
                .filter(r -> r.getRatingValue() <= 2)
                .count();
        
        if (lowRatings > 0) {
            double lowRatingRatio = (double) lowRatings / ratings.size();
            if (lowRatingRatio >= 0.3) {
                adjustedScore -= 0.5; // Penalty for 30%+ low ratings
            } else if (lowRatingRatio >= 0.15) {
                adjustedScore -= 0.3; // Penalty for 15%+ low ratings
            }
        }
        
        // Longevity bonus: ratings spread over time indicate sustained good behavior
        if (ratings.size() >= 5) {
            LocalDateTime oldest = ratings.stream()
                    .map(Rating::getCreatedDate)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            
            long daysSinceFirst = ChronoUnit.DAYS.between(oldest, LocalDateTime.now());
            if (daysSinceFirst >= 90) { // 3+ months of ratings
                adjustedScore += 0.1; // Small longevity bonus
            }
        }
        
        return Math.max(0.0, Math.min(5.0, adjustedScore));
    }
    
    /**
     * Get trust score category based on score value.
     * 
     * @param trustScore Trust score (0-5)
     * @return Trust category description
     */
    public String getTrustCategory(double trustScore) {
        if (trustScore >= 4.5) {
            return "Excellent";
        } else if (trustScore >= 3.5) {
            return "High";
        } else if (trustScore >= 2.5) {
            return "Medium";
        } else {
            return "Low";
        }
    }
    
    /**
     * Get detailed trust score breakdown for debugging/transparency.
     * 
     * @param ratings List of ratings
     * @return TrustScoreBreakdown with component scores
     */
    public TrustScoreBreakdown calculateTrustScoreBreakdown(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return new TrustScoreBreakdown(5.0, 5.0, 5.0, 5.0, 5.0, 5.0);
        }
        
        double baseScore = calculateBaseScore(ratings);
        double volumeScore = calculateVolumeScore(ratings);
        double recencyScore = calculateRecencyScore(ratings);
        double consistencyScore = calculateConsistencyScore(ratings);
        double verificationScore = calculateVerificationScore(ratings);
        
        double finalScore = (baseScore * BASE_WEIGHT) +
                          (volumeScore * VOLUME_WEIGHT) +
                          (recencyScore * RECENCY_WEIGHT) +
                          (consistencyScore * CONSISTENCY_WEIGHT) +
                          (verificationScore * VERIFICATION_WEIGHT);
        
        finalScore = applyBonusesAndPenalties(finalScore, ratings);
        
        return new TrustScoreBreakdown(
            Math.round(finalScore * 10.0) / 10.0,
            Math.round(baseScore * 10.0) / 10.0,
            Math.round(volumeScore * 10.0) / 10.0,
            Math.round(recencyScore * 10.0) / 10.0,
            Math.round(consistencyScore * 10.0) / 10.0,
            Math.round(verificationScore * 10.0) / 10.0
        );
    }
    
    /**
     * Inner class for detailed trust score breakdown.
     */
    public static class TrustScoreBreakdown {
        private final double finalScore;
        private final double baseScore;
        private final double volumeScore;
        private final double recencyScore;
        private final double consistencyScore;
        private final double verificationScore;
        
        public TrustScoreBreakdown(double finalScore, double baseScore, double volumeScore,
                                  double recencyScore, double consistencyScore, double verificationScore) {
            this.finalScore = finalScore;
            this.baseScore = baseScore;
            this.volumeScore = volumeScore;
            this.recencyScore = recencyScore;
            this.consistencyScore = consistencyScore;
            this.verificationScore = verificationScore;
        }
        
        // Getters
        public double getFinalScore() { return finalScore; }
        public double getBaseScore() { return baseScore; }
        public double getVolumeScore() { return volumeScore; }
        public double getRecencyScore() { return recencyScore; }
        public double getConsistencyScore() { return consistencyScore; }
        public double getVerificationScore() { return verificationScore; }
        
        @Override
        public String toString() {
            return String.format(
                "TrustScore: %.1f (Base: %.1f, Volume: %.1f, Recency: %.1f, Consistency: %.1f, Verification: %.1f)",
                finalScore, baseScore, volumeScore, recencyScore, consistencyScore, verificationScore
            );
        }
    }
}
