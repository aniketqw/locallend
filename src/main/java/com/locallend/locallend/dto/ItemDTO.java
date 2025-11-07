package com.locallend.locallend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ItemDTO {
    private String id;
    private String name;
    private String description;
    private String condition;
    private String status;
    private double deposit;
    private List<String> images;
    private double averageRating;
    private String ownerId;
    private String ownerName;
    private String categoryId;
    private String categoryName;
    private boolean canBeBorrowed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isCanBeBorrowed() { return canBeBorrowed; }
    public void setCanBeBorrowed(boolean canBeBorrowed) { this.canBeBorrowed = canBeBorrowed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
