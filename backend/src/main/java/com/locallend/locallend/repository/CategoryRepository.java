package com.locallend.locallend.repository;

import com.locallend.locallend.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Optional<Category> findByNameIgnoreCase(String name);

    @Query("{ 'is_active': true }")
    List<Category> findAllActiveCategories(Sort sort);

    @Query("{ 'parent_category_id': ?0, 'is_active': true }")
    List<Category> findByParentCategoryId(String parentCategoryId, Sort sort);

    @Query("{ 'parent_category_id': { $exists: false }, 'is_active': true }")
    List<Category> findRootCategories(Sort sort);

    @Query("{ $text: { $search: ?0 }, 'is_active': true }")
    List<Category> searchCategoriesByText(String searchTerm, Sort sort);

    @Query("{ 'item_count': { $gt: ?0 }, 'is_active': true }")
    List<Category> findCategoriesWithMinimumItems(long minItemCount, Sort sort);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'is_active': true }", exists = true)
    boolean existsByNameIgnoreCase(String name);

    @Query(value = "{ 'is_active': true }", count = true)
    long countActiveCategories();

    @Query("{ 'is_active': true }")
    List<Category> findAllActiveCategoriesOrderByItemCountDesc();

    List<Category> findByIsActiveTrueOrderByNameAsc();

    List<Category> findByIsActiveTrueOrderByCreatedAtDesc();

    List<Category> findByIsActiveTrueOrderByItemCountDesc();
}
