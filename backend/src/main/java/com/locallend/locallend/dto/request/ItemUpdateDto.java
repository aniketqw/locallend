package com.locallend.locallend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Partial update DTO for Item
 */
public class ItemUpdateDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("deposit")
    private Double deposit;

    @JsonProperty("images")
    private List<String> images;

    @JsonProperty("status")
    private String status;

    public ItemUpdateDto() {}

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
