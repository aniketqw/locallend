package com.locallend.locallend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CategoryDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("parent_category_id")
    private String parentCategoryId;

    @JsonProperty("parent_category_name")
    private String parentCategoryName;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("item_count")
    private long itemCount;

    @JsonProperty("has_subcategories")
    private boolean hasSubcategories;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public CategoryDto() {}

    public CategoryDto(String id, String name, String description, String parentCategoryId,
                       String parentCategoryName, boolean isActive, long itemCount,
                       boolean hasSubcategories, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.parentCategoryName = parentCategoryName;
        this.isActive = isActive;
        this.itemCount = itemCount;
        this.hasSubcategories = hasSubcategories;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private CategoryDto categoryDto = new CategoryDto();

        public Builder id(String id) { categoryDto.id = id; return this; }
        public Builder name(String name) { categoryDto.name = name; return this; }
        public Builder description(String description) { categoryDto.description = description; return this; }
        public Builder parentCategoryId(String parentCategoryId) { categoryDto.parentCategoryId = parentCategoryId; return this; }
        public Builder parentCategoryName(String parentCategoryName) { categoryDto.parentCategoryName = parentCategoryName; return this; }
        public Builder isActive(boolean isActive) { categoryDto.isActive = isActive; return this; }
        public Builder itemCount(long itemCount) { categoryDto.itemCount = itemCount; return this; }
        public Builder hasSubcategories(boolean hasSubcategories) { categoryDto.hasSubcategories = hasSubcategories; return this; }
        public Builder createdAt(LocalDateTime createdAt) { categoryDto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { categoryDto.updatedAt = updatedAt; return this; }
        public CategoryDto build() { return categoryDto; }
    }

    public static Builder builder() { return new Builder(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(String parentCategoryId) { this.parentCategoryId = parentCategoryId; }

    public String getParentCategoryName() { return parentCategoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getItemCount() { return itemCount; }
    public void setItemCount(long itemCount) { this.itemCount = itemCount; }

    public boolean isHasSubcategories() { return hasSubcategories; }
    public void setHasSubcategories(boolean hasSubcategories) { this.hasSubcategories = hasSubcategories; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
