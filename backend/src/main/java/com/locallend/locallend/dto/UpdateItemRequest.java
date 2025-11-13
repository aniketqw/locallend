package com.locallend.locallend.dto;

import java.util.List;

public class UpdateItemRequest {
    private String name;
    private String description;
    private String condition;
    private Double deposit;
    private List<String> images;
    private Boolean isAvailable;

    public UpdateItemRequest() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
