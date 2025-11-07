package com.locallend.locallend.repository;

import com.locallend.locallend.model.Category;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends MongoRepository<Item, String> {

    // Owner-based queries
    Page<Item> findByOwner(User owner, Pageable pageable);
    List<Item> findByOwnerAndIsActiveTrueAndStatus(User owner, String status, Sort sort);

    @Query("{ 'owner.$id': ?0 }")
    List<Item> findByOwnerId(String ownerId, Sort sort);

    // Availability queries
    Page<Item> findByIsActiveTrueAndStatus(String status, Pageable pageable);

    Page<Item> findByIsActiveTrueAndCategory(Category category, Pageable pageable);

    // Search across title/description/tags with paging
    @Query("{ $and: [ {'is_active': true}, { $or: [ {'name': { $regex: ?0, $options: 'i' }}, {'description': { $regex: ?0, $options: 'i' }}, {'images': { $in: [{ $regex: ?0, $options: 'i' }] } } ] } ] }")
    Page<Item> searchAvailableItems(String searchTerm, Pageable pageable);

    // Legacy list-style queries
    @Query("{ 'is_active': true, 'status': 'AVAILABLE' }")
    List<Item> findAllAvailable(Sort sort);

    @Query("{ $text: { $search: ?0 }, 'is_active': true }")
    List<Item> searchByText(String term, Sort sort);

    List<Item> findByStatusIn(List<String> statuses, Sort sort);

    List<Item> findByCondition(String condition, Sort sort);

    @Query("{ 'deposit': { $gte: ?0, $lte: ?1 }, 'is_active': true }")
    List<Item> findByDepositBetween(double min, double max, Sort sort);

    @Query("{ 'is_active': true }")
    long countActiveItems();

    // Lightweight derived queries
    List<Item> findByIsActiveTrueOrderByCreatedAtDesc();

}
