package com.locallend.locallend.service;

import com.locallend.locallend.dto.CategoryDto;
import com.locallend.locallend.dto.CreateCategoryRequest;
import com.locallend.locallend.exception.CategoryNotFoundException;
import com.locallend.locallend.model.Category;
import com.locallend.locallend.repository.CategoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto createCategory(CreateCategoryRequest request) {
        logger.info("Creating category with name: {}", request.getName());

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        Category parentCategory = null;
        if (request.hasParentCategory()) {
            parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> CategoryNotFoundException.byId(request.getParentCategoryId()));
        }

        Category category = new Category(request.getName(), request.getDescription(), request.getParentCategoryId());
        Category savedCategory = categoryRepository.save(category);

        logger.info("Category created successfully with ID: {}", savedCategory.getId());
        return convertToDto(savedCategory, parentCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(String sortBy) {
        logger.info("Fetching all categories with sort: {}", sortBy);

        Sort sort = createSort(sortBy);
        List<Category> categories = categoryRepository.findAllActiveCategories(sort);

        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(String categoryId) {
        logger.info("Fetching category by ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryNotFoundException.byId(categoryId));

        if (!category.isActive()) {
            throw new CategoryNotFoundException("Category with ID '" + categoryId + "' is not active", categoryId, null);
        }

        return convertToDto(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories(String sortBy) {
        logger.info("Fetching root categories with sort: {}", sortBy);

        Sort sort = createSort(sortBy);
        List<Category> rootCategories = categoryRepository.findRootCategories(sort);

        return rootCategories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getSubcategories(String parentCategoryId, String sortBy) {
        logger.info("Fetching subcategories for parent ID: {}", parentCategoryId);

        Category parentCategory = categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> CategoryNotFoundException.byId(parentCategoryId));

        Sort sort = createSort(sortBy);
        List<Category> subcategories = categoryRepository.findByParentCategoryId(parentCategoryId, sort);

        return subcategories.stream()
                .map(category -> convertToDto(category, parentCategory))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategories(String searchTerm, String sortBy) {
        logger.info("Searching categories with term: {}", searchTerm);

        Sort sort = createSort(sortBy);
        List<Category> categories = categoryRepository.searchCategoriesByText(searchTerm, sort);

        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getPopularCategories(long minItemCount) {
        logger.info("Fetching popular categories with minimum {} items", minItemCount);

        Sort sort = Sort.by(Sort.Direction.DESC, "item_count");
        List<Category> categories = categoryRepository.findCategoriesWithMinimumItems(minItemCount, sort);

        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CategoryDto updateCategoryStatus(String categoryId, boolean isActive) {
        logger.info("Updating category {} status to: {}", categoryId, isActive);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryNotFoundException.byId(categoryId));

        category.setActive(isActive);
        Category updatedCategory = categoryRepository.save(category);

        logger.info("Category {} status updated successfully", categoryId);
        return convertToDto(updatedCategory);
    }

    public void incrementItemCount(String categoryId) {
        logger.debug("Incrementing item count for category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryNotFoundException.byId(categoryId));

        category.incrementItemCount();
        categoryRepository.save(category);
    }

    public void decrementItemCount(String categoryId) {
        logger.debug("Decrementing item count for category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryNotFoundException.byId(categoryId));

        category.decrementItemCount();
        categoryRepository.save(category);
    }

    private CategoryDto convertToDto(Category category) {
        return convertToDto(category, null);
    }

    private CategoryDto convertToDto(Category category, Category parentCategory) {
        String parentCategoryName = null;
        if (category.hasParent() && parentCategory != null) {
            parentCategoryName = parentCategory.getName();
        } else if (category.hasParent()) {
            Optional<Category> parent = categoryRepository.findById(category.getParentCategoryId());
            parentCategoryName = parent.map(Category::getName).orElse(null);
        }

        boolean hasSubcategories = !categoryRepository.findByParentCategoryId(
                category.getId(), Sort.by("name")).isEmpty();

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategoryId())
                .parentCategoryName(parentCategoryName)
                .isActive(category.isActive())
                .itemCount(category.getItemCount())
                .hasSubcategories(hasSubcategories)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private Sort createSort(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        switch (sortBy.toLowerCase()) {
            case "name":
            case "name_asc":
                return Sort.by(Sort.Direction.ASC, "name");
            case "name_desc":
                return Sort.by(Sort.Direction.DESC, "name");
            case "created":
            case "created_desc":
                return Sort.by(Sort.Direction.DESC, "created_at");
            case "created_asc":
                return Sort.by(Sort.Direction.ASC, "created_at");
            case "popular":
            case "item_count_desc":
                return Sort.by(Sort.Direction.DESC, "item_count");
            case "item_count_asc":
                return Sort.by(Sort.Direction.ASC, "item_count");
            default:
                return Sort.by(Sort.Direction.ASC, "name");
        }
    }
}
