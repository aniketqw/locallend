package com.locallend.locallend.util;

import com.locallend.locallend.dto.request.ItemRequestDto;
import com.locallend.locallend.dto.request.ItemUpdateDto;
import com.locallend.locallend.dto.response.ItemResponseDto;
import com.locallend.locallend.dto.response.ItemSummaryDto;
import com.locallend.locallend.model.Category;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.model.enums.ItemCondition;
import com.locallend.locallend.model.enums.ItemStatus;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemResponseDto toResponseDto(Item item) {
        if (item == null) return null;
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setCondition(item.getCondition() != null ? item.getCondition().name() : null);
        dto.setStatus(item.getStatus() != null ? item.getStatus().name() : null);
        dto.setDeposit(item.getDeposit());
        dto.setImages(item.getImages());
        dto.setAverageRating(item.getAverageRating());

        User owner = item.getOwner();
        if (owner != null) {
            dto.setOwnerId(owner.getId());
            dto.setOwnerName(owner.getName());
        }

        Category cat = item.getCategory();
        if (cat != null) {
            dto.setCategoryId(cat.getId());
            dto.setCategoryName(cat.getName());
        }

        dto.setCanBeBorrowed(item.canBeBorrowed());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    public static ItemSummaryDto toSummaryDto(Item item) {
        if (item == null) return null;
        ItemSummaryDto s = new ItemSummaryDto();
        s.setId(item.getId());
        s.setName(item.getName());
        s.setThumbnail(item.getImages() != null && !item.getImages().isEmpty() ? item.getImages().get(0) : null);
        s.setCategoryName(item.getCategory() != null ? item.getCategory().getName() : null);
        s.setOwnerName(item.getOwner() != null ? item.getOwner().getName() : null);
        s.setStatus(item.getStatus() != null ? item.getStatus().name() : null);
        s.setCondition(item.getCondition() != null ? item.getCondition().name() : null);
        s.setDeposit(item.getDeposit());
        s.setAverageRating(item.getAverageRating());
        return s;
    }

    public static Item fromRequestDto(ItemRequestDto req, User owner, Category category) {
        if (req == null) return null;
        Item item = new Item();
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        ItemCondition cond = ItemCondition.fromString(req.getCondition());
        if (cond != null) item.setCondition(cond);
        item.setDeposit(req.getDeposit());
        item.setImages(req.getImages());
        item.setOwner(owner);
        item.setCategory(category);
        return item;
    }

    public static void updateFromDto(Item item, ItemUpdateDto dto) {
        if (item == null || dto == null) return;
        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getCondition() != null) {
            ItemCondition cond = ItemCondition.fromString(dto.getCondition());
            if (cond != null) item.setCondition(cond);
        }
        if (dto.getDeposit() != null) item.setDeposit(dto.getDeposit());
        if (dto.getImages() != null) item.setImages(dto.getImages());
        if (dto.getStatus() != null) {
            ItemStatus st = ItemStatus.fromString(dto.getStatus());
            if (st != null) item.setStatus(st);
        }
    }

    public static List<ItemSummaryDto> toSummaryList(List<Item> items) {
        return items == null ? List.of() : items.stream().map(ItemMapper::toSummaryDto).collect(Collectors.toList());
    }

    public static List<ItemResponseDto> toResponseList(List<Item> items) {
        return items == null ? List.of() : items.stream().map(ItemMapper::toResponseDto).collect(Collectors.toList());
    }
}
