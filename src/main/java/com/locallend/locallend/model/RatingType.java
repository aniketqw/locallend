package com.locallend.locallend.model;

/**
 * Enum defining different types of ratings in the LocalLend system.
 * Supports bidirectional ratings for users and items.
 */
public enum RatingType {
    USER_TO_USER("User to User", "Rating between users"),
    USER_TO_ITEM("User to Item", "Rating of item quality"),
    OWNER_TO_BORROWER("Owner to Borrower", "Owner rating borrower"),
    BORROWER_TO_OWNER("Borrower to Owner", "Borrower rating owner");
    
    private final String displayName;
    private final String description;
    
    RatingType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if this rating type affects user trust score.
     * @return true if rating impacts trust score calculation
     */
    public boolean affectsTrustScore() {
        return this == USER_TO_USER || this == OWNER_TO_BORROWER || this == BORROWER_TO_OWNER;
    }
    
    /**
     * Check if this rating type affects item quality score.
     * @return true if rating impacts item quality calculation
     */
    public boolean affectsItemQuality() {
        return this == USER_TO_ITEM;
    }
}
