package com.locallend.locallend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateItemRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private String categoryId;

    @DecimalMin(value = "0.0", inclusive = true)
    private Double deposit = 0.0;

    private List<String> images;

    private String condition; // NEW|EXCELLENT|GOOD|FAIR|POOR

    public CreateItemRequest() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
