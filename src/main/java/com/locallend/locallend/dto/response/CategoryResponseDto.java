package com.locallend.locallend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryResponseDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("item_count")
    private long itemCount;

    @JsonProperty("is_active")
    private boolean isActive;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getItemCount() { return itemCount; }
    public void setItemCount(long itemCount) { this.itemCount = itemCount; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
