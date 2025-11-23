package com.locallend.locallend.service.booking;

import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service dedicated to booking queries (read operations).
 * Follows Single Responsibility Principle and CQRS pattern.
 * Part of the refactored architecture - separates queries from commands.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingQueryService {

    private final BookingRepository bookingRepository;

    /**
     * Gets a booking by ID.
     */
    public BookingResponseDto getBookingById(String bookingId) {
        log.debug("Fetching booking by ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        return BookingMapper.toResponseDto(booking);
    }

    /**
     * Gets all bookings for a borrower.
     */
    public List<BookingResponseDto> getBookingsForBorrower(String borrowerId) {
        log.debug("Fetching bookings for borrower: {}", borrowerId);

        List<Booking> bookings = bookingRepository.findByBorrowerId(borrowerId);

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets all bookings for an owner.
     */
    public List<BookingResponseDto> getBookingsForOwner(String ownerId) {
        log.debug("Fetching bookings for owner: {}", ownerId);

        List<Booking> bookings = bookingRepository.findByOwnerId(ownerId);

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets bookings by status for a specific user.
     */
    public List<BookingResponseDto> getBookingsByStatus(BookingStatus status, String userId) {
        log.debug("Fetching bookings with status {} for user: {}", status, userId);

        List<Booking> bookings = bookingRepository.findByStatus(status);

        // Filter by user (as borrower or owner)
        return bookings.stream()
                .filter(b -> b.getBorrowerId().equals(userId) || b.getOwnerId().equals(userId))
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets bookings by status with pagination.
     */
    public Page<BookingResponseDto> getBookingsByStatusPaged(BookingStatus status, Pageable pageable) {
        log.debug("Fetching paged bookings with status: {}", status);

        Page<Booking> bookings = bookingRepository.findByStatus(status, pageable);

        return bookings.map(BookingMapper::toResponseDto);
    }

    /**
     * Gets all active bookings (CONFIRMED or ACTIVE status).
     */
    public List<BookingResponseDto> getActiveBookings() {
        log.debug("Fetching all active bookings");

        List<Booking> activeBookings = bookingRepository.findActiveBookings();

        return activeBookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets all overdue bookings.
     */
    public List<BookingResponseDto> getOverdueBookings() {
        log.debug("Fetching overdue bookings");

        LocalDateTime now = LocalDateTime.now();
        List<Booking> overdueBookings = bookingRepository.findOverdueBookings(now);

        return overdueBookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets active bookings for a specific item.
     */
    public List<BookingResponseDto> getActiveBookingsForItem(String itemId) {
        log.debug("Fetching active bookings for item: {}", itemId);

        List<Booking> bookings = bookingRepository.findByItemId(itemId);

        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACTIVE ||
                           b.getStatus() == BookingStatus.CONFIRMED)
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets pending bookings for an owner.
     */
    public List<BookingResponseDto> getPendingBookingsForOwner(String ownerId) {
        log.debug("Fetching pending bookings for owner: {}", ownerId);

        List<Booking> bookings = bookingRepository.findByOwnerId(ownerId);

        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets upcoming bookings for a borrower.
     */
    public List<BookingResponseDto> getUpcomingBookingsForBorrower(String borrowerId) {
        log.debug("Fetching upcoming bookings for borrower: {}", borrowerId);

        List<Booking> bookings = bookingRepository.findByBorrowerId(borrowerId);
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED &&
                           b.getStartDate().isAfter(now))
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets completed bookings for rating purposes.
     */
    public List<BookingResponseDto> getCompletedBookingsForRating(String userId) {
        log.debug("Fetching completed unrated bookings for user: {}", userId);

        List<Booking> bookings = bookingRepository.findByBorrowerId(userId);
        bookings.addAll(bookingRepository.findByOwnerId(userId));

        return bookings.stream()
                .distinct()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED && !b.getIsRated())
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Counts bookings by status for a user.
     */
    public BookingStatistics getBookingStatistics(String userId) {
        log.debug("Calculating booking statistics for user: {}", userId);

        List<Booking> allBookings = bookingRepository.findByBorrowerId(userId);
        allBookings.addAll(bookingRepository.findByOwnerId(userId));

        BookingStatistics stats = new BookingStatistics();
        stats.setTotalBookings(allBookings.size());
        stats.setPendingCount(countByStatus(allBookings, BookingStatus.PENDING));
        stats.setConfirmedCount(countByStatus(allBookings, BookingStatus.CONFIRMED));
        stats.setActiveCount(countByStatus(allBookings, BookingStatus.ACTIVE));
        stats.setCompletedCount(countByStatus(allBookings, BookingStatus.COMPLETED));
        stats.setCancelledCount(countByStatus(allBookings, BookingStatus.CANCELLED));
        stats.setRejectedCount(countByStatus(allBookings, BookingStatus.REJECTED));
        stats.setOverdueCount(countByStatus(allBookings, BookingStatus.OVERDUE));

        return stats;
    }

    private long countByStatus(List<Booking> bookings, BookingStatus status) {
        return bookings.stream()
                .filter(b -> b.getStatus() == status)
                .count();
    }

    /**
     * DTO for booking statistics.
     */
    public static class BookingStatistics {
        private long totalBookings;
        private long pendingCount;
        private long confirmedCount;
        private long activeCount;
        private long completedCount;
        private long cancelledCount;
        private long rejectedCount;
        private long overdueCount;

        // Getters and setters
        public long getTotalBookings() { return totalBookings; }
        public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

        public long getPendingCount() { return pendingCount; }
        public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }

        public long getConfirmedCount() { return confirmedCount; }
        public void setConfirmedCount(long confirmedCount) { this.confirmedCount = confirmedCount; }

        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }

        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

        public long getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }

        public long getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }

        public long getOverdueCount() { return overdueCount; }
        public void setOverdueCount(long overdueCount) { this.overdueCount = overdueCount; }
    }
}