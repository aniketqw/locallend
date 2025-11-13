package com.locallend.locallend.model;

import com.locallend.locallend.model.enums.ItemCondition;
import com.locallend.locallend.model.enums.ItemStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "items")
public class Item {
    @Id
    private String id;

    @Field("name")
    @TextIndexed
    private String name;

    @Field("description")
    @TextIndexed
    private String description;

    @Field("condition")
    private ItemCondition condition = ItemCondition.GOOD;

    @Field("status")
    @Indexed
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Field("deposit")
    private double deposit = 0.0;

    @Field("images")
    private List<String> images = new ArrayList<>();

    @Field("ratings")
    private List<Integer> ratings = new ArrayList<>();

    @DBRef(lazy = true)
    @Field("owner")
    private User owner;

    @DBRef(lazy = true)
    @Field("category")
    private Category category;

    @Field("is_active")
    private boolean isActive = true;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public Item() {}

    public Item(String name, String description, ItemCondition condition, double deposit) {
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.deposit = deposit;
        this.status = ItemStatus.AVAILABLE;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemCondition getCondition() { return condition; }
    public void setCondition(ItemCondition condition) { this.condition = condition; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public List<Integer> getRatings() { return ratings; }
    public void setRatings(List<Integer> ratings) { this.ratings = ratings; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business methods
    public boolean canBeBorrowed() {
        return isActive && status == ItemStatus.AVAILABLE;
    }

    public void markAsBorrowed() {
        if (canBeBorrowed()) {
            this.status = ItemStatus.BORROWED;
        } else {
            throw new IllegalStateException("Item cannot be borrowed in its current state");
        }
    }

    public void updateRating(int rating) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be 1-5");
        this.ratings.add(rating);
    }

    public double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", condition=" + condition +
                '}';
    }
}
