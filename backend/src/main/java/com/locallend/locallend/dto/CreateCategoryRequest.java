package com.locallend.locallend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s&-_.()]+$", message = "Category name contains invalid characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    @JsonProperty("description")
    private String description;

    @Size(max = 24, message = "Invalid parent category ID format")
    @JsonProperty("parent_category_id")
    private String parentCategoryId;

    public CreateCategoryRequest() {}

    public CreateCategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CreateCategoryRequest(String name, String description, String parentCategoryId) {
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name.trim() : null; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description.trim() : null; }

    public String getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(String parentCategoryId) { this.parentCategoryId = parentCategoryId != null ? parentCategoryId.trim() : null; }

    public boolean hasParentCategory() { return parentCategoryId != null && !parentCategoryId.isEmpty(); }
    public boolean hasDescription() { return description != null && !description.isEmpty(); }

    @Override
    public String toString() {
        return "CreateCategoryRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parentCategoryId='" + parentCategoryId + '\'' +
                '}';
    }
}
