package com.locallend.locallend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ItemRequestDto {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 100)
    @JsonProperty("name")
    private String name;

    @Size(max = 1000)
    @JsonProperty("description")
    private String description;

    @JsonProperty("condition")
    private String condition;

    @Min(value = 0, message = "Deposit must be non-negative")
    @JsonProperty("deposit")
    private double deposit = 0.0;

    @JsonProperty("images")
    private List<String> images;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("owner_id")
    private String ownerId;

    public ItemRequestDto() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
}
