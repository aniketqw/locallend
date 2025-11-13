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
        Booking booking = new Booking(item, borrower, owner, startDate, endDate);
        booking.setBookingNotes(bookingNotes);
        booking.setStatus(BookingStatus.PENDING);
        if (item.getDeposit() > 0) {
            booking.setDepositAmount(item.getDeposit());
        }
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
        Booking rescheduled = new Booking(originalBooking.getItem(), originalBooking.getBorrower(),
                originalBooking.getOwner(), newStartDate, newEndDate);
        rescheduled.setDepositAmount(originalBooking.getDepositAmount());
        rescheduled.setDepositPaid(originalBooking.getDepositPaid());
        String notes = originalBooking.getBookingNotes() != null ? originalBooking.getBookingNotes() + " " : "";
        rescheduled.setBookingNotes(notes + "[RESCHEDULED: " + reason + "]");
        rescheduled.calculateDuration();
        return rescheduled;
    }

    public Booking createTestBooking(String itemId, String borrowerId, String ownerId,
                                     BookingStatus status, LocalDateTime startDate,
                                     LocalDateTime endDate) {
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setBorrowerId(borrowerId);
        booking.setOwnerId(ownerId);
        booking.setStatus(status);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setBookingNotes("Test booking created by BookingFactory");
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
