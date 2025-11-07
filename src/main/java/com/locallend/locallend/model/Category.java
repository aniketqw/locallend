package com.locallend.locallend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Represents a category for organizing tools and items in the LocalLend platform.
 * Categories help users classify and find items efficiently.
 * Supports hierarchical structure with parent-child relationships.
 */
@Document(collection = "categories")
public class Category {
    @Id
    private String id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Indexed(unique = true)
    @TextIndexed
    @Field("name")
    private String name;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    @TextIndexed
    @Field("description")
    private String description;

    @Field("parent_category_id")
    private String parentCategoryId;

    @Field("is_active")
    private boolean isActive = true;

    @Field("item_count")
    private long itemCount = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.itemCount = 0;
    }

    public Category(String name, String description, String parentCategoryId) {
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.isActive = true;
        this.itemCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getItemCount() {
        return itemCount;
    }

    public void setItemCount(long itemCount) {
        this.itemCount = itemCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void incrementItemCount() {
        this.itemCount++;
    }

    public void decrementItemCount() {
        if (this.itemCount > 0) {
            this.itemCount--;
        }
    }

    public boolean hasParent() {
        return parentCategoryId != null && !parentCategoryId.isEmpty();
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parentCategoryId='" + parentCategoryId + '\'' +
                ", isActive=" + isActive +
                ", itemCount=" + itemCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
