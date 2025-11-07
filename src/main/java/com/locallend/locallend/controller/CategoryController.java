package com.locallend.locallend.controller;

import com.locallend.locallend.dto.CategoryDto;
import com.locallend.locallend.dto.CreateCategoryRequest;
import com.locallend.locallend.exception.CategoryNotFoundException;
import com.locallend.locallend.service.CategoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        logger.info("Request to create category: {}", request.getName());

        try {
            CategoryDto createdCategory = categoryService.createCategory(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category created successfully");
            response.put("data", createdCategory);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid category creation request: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error_code", "CATEGORY_ALREADY_EXISTS");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (CategoryNotFoundException e) {
            logger.error("Parent category not found: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Parent category not found");
            errorResponse.put("error_code", "PARENT_CATEGORY_NOT_FOUND");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(value = "sort", defaultValue = "name") String sortBy) {
        logger.info("Request to get all categories with sort: {}", sortBy);

        try {
            List<CategoryDto> categories = categoryService.getAllCategories(sortBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categories retrieved successfully");
            response.put("data", categories);
            response.put("count", categories.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving categories: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving categories");
            errorResponse.put("error_code", "INTERNAL_ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(
            @PathVariable @NotBlank(message = "Category ID is required") String categoryId) {
        logger.info("Request to get category by ID: {}", categoryId);

        try {
            CategoryDto category = categoryService.getCategoryById(categoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category retrieved successfully");
            response.put("data", category);

            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            logger.error("Category not found: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error_code", "CATEGORY_NOT_FOUND");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/root")
    public ResponseEntity<?> getRootCategories(
            @RequestParam(value = "sort", defaultValue = "name") String sortBy) {
        logger.info("Request to get root categories with sort: {}", sortBy);

        try {
            List<CategoryDto> rootCategories = categoryService.getRootCategories(sortBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Root categories retrieved successfully");
            response.put("data", rootCategories);
            response.put("count", rootCategories.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving root categories: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving root categories");
            errorResponse.put("error_code", "INTERNAL_ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<?> getSubcategories(
            @PathVariable @NotBlank(message = "Parent category ID is required") String parentId,
            @RequestParam(value = "sort", defaultValue = "name") String sortBy) {
        logger.info("Request to get subcategories for parent: {}", parentId);

        try {
            List<CategoryDto> subcategories = categoryService.getSubcategories(parentId, sortBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subcategories retrieved successfully");
            response.put("data", subcategories);
            response.put("count", subcategories.size());
            response.put("parent_id", parentId);

            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            logger.error("Parent category not found: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error_code", "PARENT_CATEGORY_NOT_FOUND");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam @NotBlank(message = "Search term is required") String q,
            @RequestParam(value = "sort", defaultValue = "name") String sortBy) {
        logger.info("Request to search categories with term: {}", q);

        try {
            List<CategoryDto> categories = categoryService.searchCategories(q, sortBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categories search completed successfully");
            response.put("data", categories);
            response.put("count", categories.size());
            response.put("search_term", q);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching categories: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error searching categories");
            errorResponse.put("error_code", "SEARCH_ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularCategories(
            @RequestParam(value = "min_items", defaultValue = "1") long minItems) {
        logger.info("Request to get popular categories with minimum {} items", minItems);

        try {
            List<CategoryDto> popularCategories = categoryService.getPopularCategories(minItems);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Popular categories retrieved successfully");
            response.put("data", popularCategories);
            response.put("count", popularCategories.size());
            response.put("min_items", minItems);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving popular categories: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving popular categories");
            errorResponse.put("error_code", "INTERNAL_ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PatchMapping("/{categoryId}/status")
    public ResponseEntity<?> updateCategoryStatus(
            @PathVariable @NotBlank(message = "Category ID is required") String categoryId,
            @RequestParam boolean is_active) {
        logger.info("Request to update category {} status to: {}", categoryId, is_active);

        try {
            CategoryDto updatedCategory = categoryService.updateCategoryStatus(categoryId, is_active);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category status updated successfully");
            response.put("data", updatedCategory);

            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            logger.error("Category not found: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error_code", "CATEGORY_NOT_FOUND");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Validation failed");
        errorResponse.put("error_code", "VALIDATION_ERROR");
        errorResponse.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
