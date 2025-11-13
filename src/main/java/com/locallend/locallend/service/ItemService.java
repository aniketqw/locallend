package com.locallend.locallend.service;

import com.locallend.locallend.dto.CreateItemRequest;
import com.locallend.locallend.dto.ItemDTO;
import com.locallend.locallend.dto.UpdateItemRequest;
import com.locallend.locallend.exception.ItemNotFoundException;
import com.locallend.locallend.exception.UnauthorizedItemAccessException;
import com.locallend.locallend.model.Category;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.model.enums.ItemStatus;
import com.locallend.locallend.repository.CategoryRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository, 
                      CategoryRepository categoryRepository, ImageService imageService) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.imageService = imageService;
    }

    public ItemDTO createItem(CreateItemRequest request, String ownerId) {
        logger.info("Creating item '{}' for owner {}", request.getName(), ownerId);

        User owner = userRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setDeposit(request.getDeposit() != null ? request.getDeposit() : 0.0);
        item.setImages(request.getImages());
        if (request.getCondition() != null) {
            item.setCondition(com.locallend.locallend.model.enums.ItemCondition.fromString(request.getCondition()));
        }
        item.setOwner(owner);
        item.setCategory(category);

        Item saved = itemRepository.save(item);
        // increment owner's item count (denormalized)
        owner.setItemCount(owner.getItemCount() + 1);
        userRepository.save(owner);

        return toItemDTO(saved);
    }

    @Transactional(readOnly = true)
    public ItemDTO getItemById(String itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        return toItemDTO(item);
    }

    public ItemDTO updateItem(String itemId, UpdateItemRequest request, String currentUserId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        checkOwner(item, currentUserId);

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCondition() != null) item.setCondition(com.locallend.locallend.model.enums.ItemCondition.fromString(request.getCondition()));
        if (request.getDeposit() != null) item.setDeposit(request.getDeposit());
        
        // Handle image updates - delete removed images from Cloudinary
        if (request.getImages() != null) {
            List<String> oldImages = item.getImages();
            List<String> newImages = request.getImages();
            
            // Find images that were removed (in old but not in new)
            if (oldImages != null && !oldImages.isEmpty()) {
                List<String> removedImages = oldImages.stream()
                    .filter(oldUrl -> !newImages.contains(oldUrl))
                    .collect(java.util.stream.Collectors.toList());
                
                // Delete removed images from Cloudinary
                if (!removedImages.isEmpty()) {
                    logger.info("Deleting {} removed images from Cloudinary for item {}", 
                               removedImages.size(), itemId);
                    try {
                        int deletedCount = imageService.deleteMultipleImages(removedImages);
                        logger.info("Successfully deleted {} out of {} images from Cloudinary", 
                                   deletedCount, removedImages.size());
                    } catch (Exception e) {
                        logger.warn("Failed to delete some images from Cloudinary: {}", e.getMessage());
                        // Continue with update even if image deletion fails
                    }
                }
            }
            
            item.setImages(newImages);
        }
        
        if (request.getIsAvailable() != null) item.setActive(request.getIsAvailable());

        Item updated = itemRepository.save(item);
        return toItemDTO(updated);
    }

    public void deleteItem(String itemId, String currentUserId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        checkOwner(item, currentUserId);

        // Soft delete - only delete if it's currently active
        if (item.isActive()) {
            // Delete images from Cloudinary before soft-deleting the item
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                logger.info("Deleting {} images from Cloudinary for item {}", 
                           item.getImages().size(), itemId);
                try {
                    int deletedCount = imageService.deleteMultipleImages(item.getImages());
                    logger.info("Successfully deleted {} out of {} images from Cloudinary", 
                               deletedCount, item.getImages().size());
                } catch (Exception e) {
                    logger.error("Failed to delete images from Cloudinary for item {}: {}", 
                                itemId, e.getMessage());
                    // Continue with soft delete even if image deletion fails
                    // Images can be cleaned up manually or via scheduled task
                }
            }
            
            item.setActive(false);
            itemRepository.save(item);

            // decrement owner's item count
            User owner = item.getOwner();
            if (owner != null && owner.getItemCount() > 0) {
                owner.setItemCount(owner.getItemCount() - 1);
                userRepository.save(owner);
            }
        }
    }

    public ItemDTO reactivateItem(String itemId, String currentUserId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        checkOwner(item, currentUserId);

        // Reactivate soft-deleted item
        if (!item.isActive()) {
            item.setActive(true);
            Item saved = itemRepository.save(item);

            // increment owner's item count
            User owner = item.getOwner();
            if (owner != null) {
                owner.setItemCount(owner.getItemCount() + 1);
                userRepository.save(owner);
            }
            
            return toItemDTO(saved);
        }
        return toItemDTO(item);
    }

    public ItemDTO toggleAvailability(String itemId, String currentUserId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        checkOwner(item, currentUserId);

        // Toggle between AVAILABLE and UNAVAILABLE (don't change isActive - that's for soft delete)
        if (item.getStatus() == ItemStatus.AVAILABLE) {
            item.setStatus(ItemStatus.UNAVAILABLE);
        } else if (item.getStatus() == ItemStatus.UNAVAILABLE) {
            item.setStatus(ItemStatus.AVAILABLE);
        }
        // Don't allow toggling if item is BORROWED
        
        Item saved = itemRepository.save(item);
        return toItemDTO(saved);
    }

    public ItemDTO setAvailability(String itemId, boolean isAvailable, String currentUserId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        checkOwner(item, currentUserId);
        item.setActive(isAvailable);
        Item saved = itemRepository.save(item);
        return toItemDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> getAvailableItems(int page, int size, String sortBy, String sortDir) {
        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable p = PageRequest.of(page, size, Sort.by(dir, sortBy == null ? "name" : sortBy));
        Page<Item> items = itemRepository.findByIsActiveTrueAndStatus("AVAILABLE", p);
        List<ItemDTO> dtos = items.stream().map(this::toItemDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, p, items.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> searchAvailableItems(String searchTerm, int page, int size) {
        Pageable p = PageRequest.of(page, size);
        Page<Item> items = itemRepository.searchAvailableItems(searchTerm, p);
        List<ItemDTO> dtos = items.stream().map(this::toItemDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, p, items.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> getAvailableItemsByCategory(String categoryId, int page, int size) {
        Category cat = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Pageable p = PageRequest.of(page, size);
        Page<Item> items = itemRepository.findByIsActiveTrueAndCategory(cat, p);
        List<ItemDTO> dtos = items.stream().map(this::toItemDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, p, items.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> getItemsByOwner(String ownerId, int page, int size) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        Pageable p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> items = itemRepository.findByOwnerAndIsActiveTrue(owner, p);
        List<ItemDTO> dtos = items.stream().map(this::toItemDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, p, items.getTotalElements());
    }

    // Helpers
    private void checkOwner(Item item, String currentUserId) {
        if (item.getOwner() == null || !item.getOwner().getId().equals(currentUserId)) {
            throw new UnauthorizedItemAccessException("User is not owner of the item");
        }
    }

    private ItemDTO toItemDTO(Item item) {
        return mapItemToDto(item);
    }

    private ItemDTO mapItemToDto(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setCondition(item.getCondition() != null ? item.getCondition().name() : null);
        dto.setStatus(item.getStatus() != null ? item.getStatus().name() : null);
        dto.setDeposit(item.getDeposit());
        dto.setImages(item.getImages());
        dto.setAverageRating(item.getAverageRating());
        if (item.getOwner() != null) {
            dto.setOwnerId(item.getOwner().getId());
            dto.setOwnerName(item.getOwner().getName());
        }
        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
            dto.setCategoryName(item.getCategory().getName());
        }
        dto.setCanBeBorrowed(item.canBeBorrowed());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}
