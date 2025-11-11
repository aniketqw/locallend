package com.locallend.locallend.util;

import com.locallend.locallend.exception.BookingConflictException;
import com.locallend.locallend.exception.InvalidBookingPeriodException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.model.enums.ItemStatus;
import com.locallend.locallend.repository.BookingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Validator for booking business rules and constraints.
 */
@Component
public class BookingValidator {

    private static final int DEFAULT_MIN_RENTAL_DAYS = 1;
    private static final int DEFAULT_MAX_RENTAL_DAYS = 90;

    /**
     * Validate that an item is available for booking.
     */
    public void validateAvailability(Item item, LocalDateTime startDate, LocalDateTime endDate) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        if (!item.isActive()) {
            throw new BookingConflictException("Item is not active and cannot be booked", item.getId());
        }

        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new BookingConflictException("Item is not available for booking (status: " + item.getStatus() + ")", item.getId());
        }

        validateBookingPeriod(startDate, endDate);
    }

    /**
     * Validate booking period against date constraints.
     */
    public void validateBookingPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (!endDate.isAfter(startDate)) {
            throw new InvalidBookingPeriodException("End date must be after start date");
        }

        if (startDate.isBefore(LocalDateTime.now().minusHours(1))) {
            throw new InvalidBookingPeriodException("Start date cannot be in the past");
        }

        long days = DateTimeUtil.calculateDaysBetween(startDate, endDate);
        
        // For now use defaults; Item model can be enhanced later with min/max rental days
        if (days < DEFAULT_MIN_RENTAL_DAYS) {
            throw new InvalidBookingPeriodException(
                "Booking period too short. Minimum " + DEFAULT_MIN_RENTAL_DAYS + " day(s) required",
                DEFAULT_MIN_RENTAL_DAYS, DEFAULT_MAX_RENTAL_DAYS, days
            );
        }

        if (days > DEFAULT_MAX_RENTAL_DAYS) {
            throw new InvalidBookingPeriodException(
                "Booking period too long. Maximum " + DEFAULT_MAX_RENTAL_DAYS + " day(s) allowed",
                DEFAULT_MIN_RENTAL_DAYS, DEFAULT_MAX_RENTAL_DAYS, days
            );
        }
    }

    /**
     * Validate no conflicting bookings exist for the item in the date range.
     */
    public void validateNoConflicts(String itemId, LocalDateTime startDate, LocalDateTime endDate,
                                    BookingRepository repository) {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID cannot be null");
        }

        List<Booking> conflicts = repository.findConflictingBookings(itemId, startDate, endDate);
        
        if (!conflicts.isEmpty()) {
            Booking conflict = conflicts.get(0);
            throw new BookingConflictException(
                "Item already booked for selected dates. Conflict with booking " + conflict.getId(),
                itemId,
                conflict.getId()
            );
        }
    }

    /**
     * Validate that user can borrow item (not their own).
     */
    public void validateBorrowerEligibility(User borrower, User owner) {
        if (borrower == null || owner == null) {
            throw new IllegalArgumentException("Borrower and owner cannot be null");
        }

        if (borrower.getId().equals(owner.getId())) {
            throw new IllegalArgumentException("User cannot borrow their own item");
        }

        // Trust score validation
        if (borrower.getTrustScore() < 0.3) {
            throw new IllegalArgumentException("Borrower trust score too low for booking (minimum 0.3 required)");
        }
    }

    /**
     * Validate that a booking can transition to a new status.
     */
    public void validateStatusTransition(Booking booking, com.locallend.locallend.model.enums.BookingStatus newStatus) {
        if (booking == null || newStatus == null) {
            throw new IllegalArgumentException("Booking and new status cannot be null");
        }

        com.locallend.locallend.model.enums.BookingStatus currentStatus = booking.getStatus();
        
        if (!canTransitionTo(currentStatus, newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    /**
     * Check if a status transition is valid.
     */
    private boolean canTransitionTo(com.locallend.locallend.model.enums.BookingStatus current,
                                   com.locallend.locallend.model.enums.BookingStatus target) {
        return switch (current) {
            case PENDING -> target == com.locallend.locallend.model.enums.BookingStatus.CONFIRMED 
                         || target == com.locallend.locallend.model.enums.BookingStatus.CANCELLED
                         || target == com.locallend.locallend.model.enums.BookingStatus.REJECTED;
            case CONFIRMED -> target == com.locallend.locallend.model.enums.BookingStatus.ACTIVE 
                           || target == com.locallend.locallend.model.enums.BookingStatus.CANCELLED;
            case ACTIVE -> target == com.locallend.locallend.model.enums.BookingStatus.COMPLETED 
                        || target == com.locallend.locallend.model.enums.BookingStatus.OVERDUE;
            case COMPLETED, CANCELLED, REJECTED, OVERDUE -> false;
        };
    }

    /**
     * Validate authorization for booking operation.
     */
    public void validateAuthorization(Booking booking, String userId, String requiredRole) {
        if (booking == null || userId == null) {
            throw new IllegalArgumentException("Booking and user ID cannot be null");
        }

        switch (requiredRole.toUpperCase()) {
            case "BORROWER":
                if (booking.getBorrower() == null || !userId.equals(booking.getBorrower().getId())) {
                    throw new IllegalArgumentException("Only the borrower can perform this action");
                }
                break;
            case "OWNER":
                if (booking.getOwner() == null || !userId.equals(booking.getOwner().getId())) {
                    throw new IllegalArgumentException("Only the owner can perform this action");
                }
                break;
            case "EITHER":
                boolean isBorrower = booking.getBorrower() != null && userId.equals(booking.getBorrower().getId());
                boolean isOwner = booking.getOwner() != null && userId.equals(booking.getOwner().getId());
                if (!isBorrower && !isOwner) {
                    throw new IllegalArgumentException("Only the borrower or owner can perform this action");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid required role: " + requiredRole);
        }
    }
}
