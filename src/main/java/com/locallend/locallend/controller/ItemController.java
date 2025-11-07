package com.locallend.locallend.controller;

import com.locallend.locallend.dto.CreateItemRequest;
import com.locallend.locallend.dto.ItemDTO;
import com.locallend.locallend.dto.UpdateItemRequest;
import com.locallend.locallend.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Item management in the LocalLend platform.
 * Provides endpoints for CRUD operations, search, filtering, and availability management.
 */
@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Create a new item
     * POST /api/items
     * Request body: CreateItemRequest (name, description, categoryId, deposit, images, condition)
     * Request header: X-User-Id (required for owner identification)
     */
    @PostMapping
    public ResponseEntity<?> createItem(
            @Valid @RequestBody CreateItemRequest request,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        try {
            ItemDTO item = itemService.createItem(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create item");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all available items with pagination and sorting
     * GET /api/items
     * Query params: page (default: 0), size (default: 10), sortBy (default: name), sortDir (default: asc)
     */
    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getAllAvailableItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Page<ItemDTO> items = itemService.getAvailableItems(page, size, sortBy, sortDir);
        return ResponseEntity.ok(items);
    }

    /**
     * Get item by ID
     * GET /api/items/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable String id) {
        try {
            ItemDTO item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Item not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Search available items
     * GET /api/items/search
     * Query params: q (search term), page (default: 0), size (default: 10)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ItemDTO>> searchItems(
            @RequestParam(required = true) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ItemDTO> items = itemService.searchAvailableItems(q, page, size);
        return ResponseEntity.ok(items);
    }

    /**
     * Get items by category
     * GET /api/items/category/{categoryId}
     * Query params: page (default: 0), size (default: 10)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getItemsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<ItemDTO> items = itemService.getAvailableItemsByCategory(categoryId, page, size);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid category");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get items by owner
     * GET /api/items/owner/{ownerId}
     * Query params: page (default: 0), size (default: 10)
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getItemsByOwner(
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<ItemDTO> items = itemService.getItemsByOwner(ownerId, page, size);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid owner");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get current user's items
     * GET /api/items/my-items
     * Request header: X-User-Id (required for authentication)
     */
    @GetMapping("/my-items")
    public ResponseEntity<?> getMyItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        try {
            Page<ItemDTO> items = itemService.getItemsByOwner(userId, page, size);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve items");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update an item
     * PUT /api/items/{id}
     * Request body: UpdateItemRequest (optional fields: name, description, condition, deposit, images, isAvailable)
     * Request header: X-User-Id (required for owner verification)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable String id,
            @Valid @RequestBody UpdateItemRequest request,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        try {
            ItemDTO updatedItem = itemService.updateItem(id, request, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (com.locallend.locallend.exception.UnauthorizedItemAccessException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access denied");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (com.locallend.locallend.exception.ItemNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Item not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update item");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Toggle item availability
     * PATCH /api/items/{id}/toggle-availability
     * Request header: X-User-Id (required for owner verification)
     */
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<?> toggleAvailability(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        try {
            ItemDTO updatedItem = itemService.toggleAvailability(id, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (com.locallend.locallend.exception.UnauthorizedItemAccessException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access denied");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (com.locallend.locallend.exception.ItemNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Item not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to toggle availability");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Set item availability
     * PATCH /api/items/{id}/availability
     * Request body: { "isAvailable": true/false }
     * Request header: X-User-Id (required for owner verification)
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<?> setAvailability(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        try {
            Boolean isAvailable = request.get("isAvailable");
            if (isAvailable == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid request");
                error.put("message", "isAvailable field is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            ItemDTO updatedItem = itemService.setAvailability(id, isAvailable, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (com.locallend.locallend.exception.UnauthorizedItemAccessException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access denied");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (com.locallend.locallend.exception.ItemNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Item not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to set availability");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete an item (soft delete)
     * DELETE /api/items/{id}
     * Request header: X-User-Id (required for owner verification)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        
        try {
            itemService.deleteItem(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Item deleted successfully");
            return ResponseEntity.ok(response);
        } catch (com.locallend.locallend.exception.UnauthorizedItemAccessException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access denied");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (com.locallend.locallend.exception.ItemNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Item not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete item");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
