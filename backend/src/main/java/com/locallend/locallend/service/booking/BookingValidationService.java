package com.locallend.locallend.service.booking;

import com.locallend.locallend.exception.*;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service dedicated to booking validation logic.
 * Follows Single Responsibility Principle - only handles validation.
 * Part of the refactored architecture using Pure Fabrication pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingValidationService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private static final double MINIMUM_TRUST_SCORE_FOR_BOOKING = 3.0;
    private static final int MAXIMUM_BOOKING_DAYS = 30;
    private static final int MAXIMUM_ADVANCE_BOOKING_DAYS = 90;

    /**
     * Validates if a booking can be created.
     */
    public void validateBookingCreation(String itemId, String borrowerId,
                                       LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Validating booking creation for item: {} by borrower: {}", itemId, borrowerId);

        // Validate dates
        validateBookingDates(startDate, endDate);

        // Validate item
        Item item = validateItemAvailability(itemId);

        // Validate borrower
        User borrower = validateBorrowerEligibility(borrowerId);

        // Validate owner-borrower relationship
        validateOwnerBorrowerRelationship(item, borrower);

        // Check for conflicts
        validateNoBookingConflicts(itemId, startDate, endDate);

        log.debug("Booking validation successful");
    }

    /**
     * Validates booking date range.
     */
    public void validateBookingDates(LocalDateTime startDate, LocalDateTime endDate) {
        // Check nulls
        if (startDate == null || endDate == null) {
            throw new InvalidBookingPeriodException("Start and end dates are required");
        }

        // Check order
        if (!endDate.isAfter(startDate)) {
            throw new InvalidBookingPeriodException("End date must be after start date");
        }

        // Check not in past
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingPeriodException("Start date cannot be in the past");
        }

        // Check maximum duration
        long durationDays = java.time.Duration.between(startDate, endDate).toDays();
        if (durationDays > MAXIMUM_BOOKING_DAYS) {
            throw new InvalidBookingPeriodException(
                    String.format("Booking duration cannot exceed %d days", MAXIMUM_BOOKING_DAYS));
        }

        // Check maximum advance booking
        long daysInAdvance = java.time.Duration.between(LocalDateTime.now(), startDate).toDays();
        if (daysInAdvance > MAXIMUM_ADVANCE_BOOKING_DAYS) {
            throw new InvalidBookingPeriodException(
                    String.format("Cannot book more than %d days in advance", MAXIMUM_ADVANCE_BOOKING_DAYS));
        }
    }

    /**
     * Validates item availability.
     */
    public Item validateItemAvailability(String itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + itemId));

        if (!item.isActive()) {
            throw new ItemNotAvailableException("Item is not active: " + itemId);
        }

        if (!item.canBeBorrowed()) {
            throw new ItemNotAvailableException("Item is not available for borrowing: " + itemId);
        }

        return item;
    }

    /**
     * Validates borrower eligibility.
     */
    public User validateBorrowerEligibility(String borrowerId) {
        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new UserNotFoundException("Borrower not found: " + borrowerId));

        if (!borrower.isActive()) {
            throw new BusinessException("Borrower account is not active");
        }

        if (borrower.getTrustScore() < MINIMUM_TRUST_SCORE_FOR_BOOKING) {
            throw new InsufficientTrustScoreException(
                    borrowerId,
                    borrower.getTrustScore(),
                    MINIMUM_TRUST_SCORE_FOR_BOOKING);
        }

        return borrower;
    }

    /**
     * Validates owner-borrower relationship.
     */
    public void validateOwnerBorrowerRelationship(Item item, User borrower) {
        if (item.getOwner() != null && item.getOwner().getId().equals(borrower.getId())) {
            throw new BusinessException("Cannot borrow your own items");
        }
    }

    /**
     * Checks for booking conflicts.
     */
    public void validateNoBookingConflicts(String itemId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                itemId, startDate, endDate);

        if (!conflictingBookings.isEmpty()) {
            log.warn("Found {} conflicting bookings for item {} in period {} to {}",
                    conflictingBookings.size(), itemId, startDate, endDate);
            throw new BookingConflictException(
                    "Item is already booked for the requested period. Conflicting bookings: " +
                            conflictingBookings.size());
        }
    }

    /**
     * Validates if a booking can be confirmed.
     */
    public void validateBookingConfirmation(Booking booking, String userId) {
        // Check ownership
        if (!booking.getOwnerId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the owner can confirm this booking");
        }

        // Check status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Can only confirm bookings in PENDING status. Current status: " + booking.getStatus());
        }

        // Check dates not passed
        if (booking.getStartDate().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingPeriodException("Cannot confirm booking with start date in the past");
        }
    }

    /**
     * Validates if a booking can be activated.
     */
    public void validateBookingActivation(Booking booking, String userId) {
        // Check borrower
        if (!booking.getBorrowerId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the borrower can activate this booking");
        }

        // Check status
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidStateTransitionException(
                    "Can only activate bookings in CONFIRMED status. Current status: " + booking.getStatus());
        }

        // Check within activation window (e.g., within 1 day of start date)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime activationWindow = booking.getStartDate().plusDays(1);
        if (now.isAfter(activationWindow)) {
            throw new InvalidBookingPeriodException("Booking activation window has passed");
        }
    }

    /**
     * Validates if a booking can be completed.
     */
    public void validateBookingCompletion(Booking booking, String userId) {
        // Check borrower
        if (!booking.getBorrowerId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the borrower can complete this booking");
        }

        // Check status
        if (booking.getStatus() != BookingStatus.ACTIVE && booking.getStatus() != BookingStatus.OVERDUE) {
            throw new InvalidStateTransitionException(
                    "Can only complete bookings in ACTIVE or OVERDUE status. Current status: " + booking.getStatus());
        }
    }

    /**
     * Validates if a booking can be cancelled.
     */
    public void validateBookingCancellation(Booking booking, String userId) {
        // Check authorization (borrower or owner)
        if (!booking.getBorrowerId().equals(userId) && !booking.getOwnerId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the borrower or owner can cancel this booking");
        }

        // Check if cancellable
        if (!booking.getStatus().canBeCancelled()) {
            throw new InvalidStateTransitionException(
                    "Cannot cancel booking in " + booking.getStatus() + " status");
        }
    }

    /**
     * Validates if a booking can be rejected.
     */
    public void validateBookingRejection(Booking booking, String userId) {
        // Check ownership
        if (!booking.getOwnerId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the owner can reject this booking");
        }

        // Check status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Can only reject bookings in PENDING status. Current status: " + booking.getStatus());
        }
    }
}