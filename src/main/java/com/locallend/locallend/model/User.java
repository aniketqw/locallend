package com.locallend.locallend.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.geo.Point;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** 
 * Represents a user in the LocalLend platform.
 * Production-ready entity with validation, indexing, and geospatial support.
 */
@Document(collection = "users")
@CompoundIndexes({
	@CompoundIndex(name = "username_unique_idx", def = "{'username': 1}", unique = true),
	@CompoundIndex(name = "email_unique_idx", def = "{'email': 1}", unique = true)
})
public class User{
	@Id
	private String id;
	
	@Indexed(unique = true)
	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
	private String username;
	
	@NotBlank(message = "Name is required")
	@Size(max = 100, message = "Name cannot exceed 100 characters")
	private String name;
	
	@Indexed(unique = true)
	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;
	
	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
	private String password;
	
	@Field("phone_number")
	@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
	private String phoneNumber;
	
	@Field("profile_image_url")
	private String profileImageUrl;
	
	@Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be USER or ADMIN")
	private String role = "USER";
	
	@Field("is_active")
	private Boolean isActive = true;
	
	@Field("created_date")
	private LocalDateTime createdDate;
    
	// Trust/reputation score for the user (0.0 - 5.0)
	@Field("trust_score")
	@DecimalMin(value = "0.0", message = "Trust score cannot be negative")
	@DecimalMax(value = "5.0", message = "Trust score cannot exceed 5.0")
	private Double trustScore = 5.0;

	// Denormalized count of items owned by the user
	@Field("item_count")
	private Long itemCount = 0L;
	
	// Geospatial location for proximity-based features
	@GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
	private Point location;
	
	// Rating-related fields (Issue #25)
	@Field("total_ratings_received")
	private Integer totalRatingsReceived = 0;

	@Field("last_trust_score_update")
	private LocalDateTime lastTrustScoreUpdate;

	@Field("rating_count_as_borrower")
	private Integer ratingCountAsBorrower = 0;

	@Field("rating_count_as_lender")
	private Integer ratingCountAsLender = 0;

	@Field("average_rating_as_borrower")
	private Double averageRatingAsBorrower = 5.0;

	@Field("average_rating_as_lender")
	private Double averageRatingAsLender = 5.0;
	
	public User(){
		this.createdDate = LocalDateTime.now();
	}

	// Constructor for Issue #4 registration
	public User(String username, String name, String email, String password){
		this();
		this.username = username;
		this.name = name;
		this.email = email;
		this.password = password;
	}

	// Legacy constructor
	public User(String name, String email){
		this();
		this.name = name;
		this.email = email;
	}
    
	public User(String name, String email, Double trustScore, Long itemCount) {
		this();
		this.name = name;
		this.email = email;
		this.trustScore = trustScore;
		this.itemCount = itemCount;
	}

	//Getters and Setters

	public String getId(){return id;}
	public void setId(String id){this.id=id;}
	
	public String getUsername(){return username;}
	public void setUsername(String username){this.username=username;}
	
	public String getName(){return name;}
	public void setName(String name){this.name=name;}
	
	public String getEmail(){return email;}
	public void setEmail(String email){this.email=email;}
	
	public String getPassword(){return password;}
	public void setPassword(String password){this.password=password;}
	
	public String getPhoneNumber(){return phoneNumber;}
	public void setPhoneNumber(String phoneNumber){this.phoneNumber=phoneNumber;}
	
	public String getProfileImageUrl(){return profileImageUrl;}
	public void setProfileImageUrl(String profileImageUrl){this.profileImageUrl=profileImageUrl;}
	
	public String getRole(){return role;}
	public void setRole(String role){this.role=role;}
	
	public Boolean getIsActive(){return isActive;}
	public void setIsActive(Boolean isActive){this.isActive=isActive;}
	
	public LocalDateTime getCreatedDate(){return createdDate;}
	public void setCreatedDate(LocalDateTime createdDate){this.createdDate=createdDate;}

	public Double getTrustScore() { return trustScore; }
	public void setTrustScore(Double trustScore) { this.trustScore = trustScore; }

	public Long getItemCount() { return itemCount; }
	public void setItemCount(Long itemCount) { this.itemCount = itemCount; }
	
	public Point getLocation() { return location; }
	public void setLocation(Point location) { this.location = location; }
	
	// Rating-related getters and setters (Issue #25)
	public Integer getTotalRatingsReceived() { return totalRatingsReceived; }
	public void setTotalRatingsReceived(Integer totalRatingsReceived) { this.totalRatingsReceived = totalRatingsReceived; }

	public LocalDateTime getLastTrustScoreUpdate() { return lastTrustScoreUpdate; }
	public void setLastTrustScoreUpdate(LocalDateTime lastTrustScoreUpdate) { this.lastTrustScoreUpdate = lastTrustScoreUpdate; }

	public Integer getRatingCountAsBorrower() { return ratingCountAsBorrower; }
	public void setRatingCountAsBorrower(Integer ratingCountAsBorrower) { this.ratingCountAsBorrower = ratingCountAsBorrower; }

	public Integer getRatingCountAsLender() { return ratingCountAsLender; }
	public void setRatingCountAsLender(Integer ratingCountAsLender) { this.ratingCountAsLender = ratingCountAsLender; }

	public Double getAverageRatingAsBorrower() { return averageRatingAsBorrower; }
	public void setAverageRatingAsBorrower(Double averageRatingAsBorrower) { this.averageRatingAsBorrower = averageRatingAsBorrower; }

	public Double getAverageRatingAsLender() { return averageRatingAsLender; }
	public void setAverageRatingAsLender(Double averageRatingAsLender) { this.averageRatingAsLender = averageRatingAsLender; }

	// Rating-related helper methods (Issue #25)
	public void incrementRatingsReceived() {
		this.totalRatingsReceived++;
	}

	public boolean hasRatings() {
		return totalRatingsReceived != null && totalRatingsReceived > 0;
	}
}
