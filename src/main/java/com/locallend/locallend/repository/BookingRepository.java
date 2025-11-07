package com.locallend.locallend.repository;

import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Booking entity providing CRUD operations and key queries.
 * (Slimmed initial version of full spec; can be extended later.)
 */
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByBorrowerId(String borrowerId);
    List<Booking> findByOwnerId(String ownerId);
    Page<Booking> findByBorrowerId(String borrowerId, Pageable pageable);
    Page<Booking> findByOwnerId(String ownerId, Pageable pageable);

    List<Booking> findByItemId(String itemId);
    List<Booking> findByItemIdAndStatus(String itemId, BookingStatus status);

    List<Booking> findByStatus(BookingStatus status);
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    List<Booking> findByBorrowerIdAndStatus(String borrowerId, BookingStatus status);
    List<Booking> findByOwnerIdAndStatus(String ownerId, BookingStatus status);

    @Query("{'status': {'$in': ['CONFIRMED', 'ACTIVE']}}")
    List<Booking> findActiveBookings();

    @Query("{'status': 'ACTIVE', 'end_date': {'$lt': ?0}}")
    List<Booking> findOverdueBookings(LocalDateTime now);

    @Query("{'item_id': ?0, 'status': {'$in': ['CONFIRMED', 'ACTIVE']}, '$or': [" +
            "{'start_date': {'$lte': ?2}, 'end_date': {'$gte': ?1}}, " +
            "{'start_date': {'$lte': ?1}, 'end_date': {'$gte': ?1}}, " +
            "{'start_date': {'$lte': ?2}, 'end_date': {'$gte': ?2}}]}")
    List<Booking> findConflictingBookings(String itemId, LocalDateTime requestedStart, LocalDateTime requestedEnd);

    // Statistics queries
    long countByItemIdAndStatus(String itemId, BookingStatus status);

    @Query("{'borrower_id': ?0, 'status': 'COMPLETED', 'end_date': {'$gte': ?1, '$lte': ?2}}")
    long countCompletedBookingsByBorrowerInPeriod(String borrowerId, LocalDateTime start, LocalDateTime end);

    @Query("{'owner_id': ?0, 'status': 'COMPLETED', 'end_date': {'$gte': ?1, '$lte': ?2}}")
    long countCompletedBookingsByOwnerInPeriod(String ownerId, LocalDateTime start, LocalDateTime end);

    // User-based queries with status
    List<Booking> findByBorrowerIdAndStatusOrderByCreatedDateDesc(String borrowerId, BookingStatus status);
    List<Booking> findByOwnerIdAndStatusOrderByCreatedDateDesc(String ownerId, BookingStatus status);
}
