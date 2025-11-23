package com.locallend.locallend.util;

import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.model.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Factory class for creating Booking objects with proper validation and business rules.
 */
@Component
public class BookingFactory {

    public Booking createBookingRequest(Item item, User borrower, User owner,
                                        LocalDateTime startDate, LocalDateTime endDate,
                                        String bookingNotes) {
        validateBookingParameters(item, borrower, owner, startDate, endDate);
        Booking booking = Booking.builder()
                .item(item)
                .itemId(item.getId())
                .borrower(borrower)
                .borrowerId(borrower.getId())
                .owner(owner)
                .ownerId(owner.getId())
                .startDate(startDate)
                .endDate(endDate)
                .bookingNotes(bookingNotes)
                .status(BookingStatus.PENDING)
                .depositAmount(item.getDeposit() > 0 ? item.getDeposit() : 0.0)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        booking.calculateDuration();
        return booking;
    }

    public Booking createBookingRequestWithDeposit(Item item, User borrower, User owner,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   String bookingNotes, Double customDepositAmount) {
        Booking booking = createBookingRequest(item, borrower, owner, startDate, endDate, bookingNotes);
        if (customDepositAmount != null && customDepositAmount >= 0) {
            booking.setDepositAmount(customDepositAmount);
        }
        return booking;
    }

    public Booking createImmediateBooking(Item item, User borrower, User owner, Integer durationDays) {
        if (durationDays == null || durationDays < 1) {
            throw new IllegalArgumentException("Duration must be at least 1 day");
        }
        LocalDateTime startDate = LocalDateTime.now().plusHours(1);
        LocalDateTime endDate = startDate.plusDays(durationDays);
        return createBookingRequest(item, borrower, owner, startDate, endDate, "Immediate booking");
    }

    public Booking createRescheduledBooking(Booking originalBooking, LocalDateTime newStartDate,
                                            LocalDateTime newEndDate, String reason) {
        validateBookingParameters(originalBooking.getItem(), originalBooking.getBorrower(),
                originalBooking.getOwner(), newStartDate, newEndDate);
        String notes = originalBooking.getBookingNotes() != null ? originalBooking.getBookingNotes() + " " : "";
        Booking rescheduled = Booking.builder()
                .item(originalBooking.getItem())
                .itemId(originalBooking.getItemId())
                .borrower(originalBooking.getBorrower())
                .borrowerId(originalBooking.getBorrowerId())
                .owner(originalBooking.getOwner())
                .ownerId(originalBooking.getOwnerId())
                .startDate(newStartDate)
                .endDate(newEndDate)
                .depositAmount(originalBooking.getDepositAmount())
                .depositPaid(originalBooking.getDepositPaid())
                .bookingNotes(notes + "[RESCHEDULED: " + reason + "]")
                .status(BookingStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        rescheduled.calculateDuration();
        return rescheduled;
    }

    public Booking createTestBooking(String itemId, String borrowerId, String ownerId,
                                     BookingStatus status, LocalDateTime startDate,
                                     LocalDateTime endDate) {
        Booking booking = Booking.builder()
                .itemId(itemId)
                .borrowerId(borrowerId)
                .ownerId(ownerId)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .bookingNotes("Test booking created by BookingFactory")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        booking.calculateDuration();
        return booking;
    }

    public Booking createValidatedBooking(Item item, User borrower, User owner,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          String bookingNotes) {
        validateBookingParameters(item, borrower, owner, startDate, endDate);
        validateBusinessRules(item, borrower, owner);
        return createBookingRequest(item, borrower, owner, startDate, endDate, bookingNotes);
    }

    private void validateBookingParameters(Item item, User borrower, User owner,
                                           LocalDateTime startDate, LocalDateTime endDate) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        if (borrower == null) throw new IllegalArgumentException("Borrower cannot be null");
        if (owner == null) throw new IllegalArgumentException("Owner cannot be null");
        if (startDate == null) throw new IllegalArgumentException("Start date cannot be null");
        if (endDate == null) throw new IllegalArgumentException("End date cannot be null");
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("Start date cannot be after end date");
        if (startDate.isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
    }

    private void validateBusinessRules(Item item, User borrower, User owner) {
        if (borrower.getId() != null && borrower.getId().equals(owner.getId())) {
            throw new IllegalArgumentException("User cannot borrow their own item");
        }
        if (!item.canBeBorrowed()) {
            throw new IllegalArgumentException("Item is not available for borrowing");
        }
        if (borrower.getTrustScore() < 0.5) {
            throw new IllegalArgumentException("Borrower trust score too low for booking");
        }
    }

    public Booking createFromRequest(Item item, User borrower, User owner,
                                     LocalDateTime startDate, LocalDateTime endDate,
                                     String notes, Double depositAmount) {
        if (depositAmount != null && depositAmount >= 0) {
            return createBookingRequestWithDeposit(item, borrower, owner, startDate, endDate, notes, depositAmount);
        }
        return createBookingRequest(item, borrower, owner, startDate, endDate, notes);
    }
}
