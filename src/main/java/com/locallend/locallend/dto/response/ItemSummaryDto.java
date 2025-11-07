package com.locallend.locallend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemSummaryDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("deposit")
    private double deposit;

    @JsonProperty("average_rating")
    private double averageRating;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}
