package com.locallend.locallend.config;

import com.locallend.locallend.model.Category;
import com.locallend.locallend.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes default categories in the database at startup.
 * This ensures that the application has basic categories available
 * immediately after deployment without manual intervention.
 */
@Component
@Profile("prod") // Only run in production profile used in docker-compose
public class CategoryDataInitializer implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryDataInitializer.class);
    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        // Check if categories already exist to avoid duplicates
        long existingCategoryCount = categoryRepository.count();
        if (existingCategoryCount > 0) {
            log.info("[CategoryInit] Categories already exist ({}), skipping initialization", existingCategoryCount);
            return;
        }

        log.info("[CategoryInit] Initializing default categories...");

        List<Category> defaultCategories = Arrays.asList(
            createCategory("Electronics", "Electronic devices, gadgets, and technology items including phones, laptops, cameras, and accessories"),
            createCategory("Tools", "Hand tools, power tools, and equipment for construction, repair, and maintenance work"),
            createCategory("Sports", "Sports equipment, gear, and accessories for various athletic activities and outdoor recreation"),
            createCategory("Books", "Books, magazines, educational materials, and reading resources across all genres and topics"),
            createCategory("Furniture", "Home and office furniture including chairs, tables, storage solutions, and decorative pieces"),
            createCategory("Vehicles", "Cars, motorcycles, bicycles, and other transportation vehicles and related accessories"),
            createCategory("Appliances", "Home appliances, kitchen equipment, and household electrical devices"),
            createCategory("Others", "Miscellaneous items that don't fit into other specific categories")
        );

        try {
            List<Category> savedCategories = categoryRepository.saveAll(defaultCategories);
            log.info("[CategoryInit] Successfully initialized {} categories", savedCategories.size());
            
            // Log each category for verification
            savedCategories.forEach(category -> 
                log.info("[CategoryInit] Created category: {} (ID: {})", category.getName(), category.getId())
            );
            
        } catch (Exception e) {
            log.error("[CategoryInit] Failed to initialize categories", e);
            throw new RuntimeException("Failed to initialize default categories", e);
        }
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }
}