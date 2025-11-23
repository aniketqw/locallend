package com.locallend.locallend.state.booking.states;

import com.locallend.locallend.exception.InvalidStateTransitionException;
import com.locallend.locallend.exception.UnauthorizedAccessException;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.model.enums.ItemStatus;
import com.locallend.locallend.state.booking.BookingState;
import com.locallend.locallend.state.booking.BookingStateContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Confirmed state - booking has been approved by owner.
 * From here, booking can be activated or cancelled.
 */
@Slf4j
public class ConfirmedState implements BookingState {

    @Override
    public void confirm(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Booking is already confirmed");
    }

    @Override
    public void activate(BookingStateContext context) {
        // Only borrower can activate (pick up the item)
        if (!context.isBorrower()) {
            throw new UnauthorizedAccessException("Only the borrower can activate the booking");
        }

        // Update booking data
        context.getBooking().setStatus(BookingStatus.ACTIVE);
        context.getBooking().setPickupDate(LocalDateTime.now());

        LocalDateTime actualStartDate = context.getMetadata("actualStartDate", LocalDateTime.class);
        if (actualStartDate != null) {
            context.getBooking().setActualStartDate(actualStartDate);
        } else {
            context.getBooking().setActualStartDate(LocalDateTime.now());
        }

        Boolean depositPaid = context.getMetadata("depositPaid", Boolean.class);
        if (depositPaid != null) {
            context.getBooking().setDepositPaid(depositPaid);
        }

        // Mark item as borrowed
        if (context.getBooking().getItem() != null) {
            context.getBooking().getItem().setStatus(ItemStatus.BORROWED);
        }

        // Transition to active state
        context.transitionTo(new ActiveState());

        log.info("Booking {} activated by borrower {}", context.getBooking().getId(), context.getUserId());
    }

    @Override
    public void complete(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot complete booking from CONFIRMED state. Booking must be ACTIVE first.");
    }

    @Override
    public void cancel(BookingStateContext context) {
        // Both borrower and owner can cancel confirmed booking
        if (!context.isBorrower() && !context.isOwner()) {
            throw new UnauthorizedAccessException("Only borrower or owner can cancel the booking");
        }

        context.getBooking().setStatus(BookingStatus.CANCELLED);
        context.getBooking().setCancelledDate(LocalDateTime.now());
        context.transitionTo(new CancelledState());

        log.info("Booking {} cancelled by user {}", context.getBooking().getId(), context.getUserId());
    }

    @Override
    public void reject(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot reject an already confirmed booking. Use cancel instead.");
    }

    @Override
    public void markOverdue(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot mark CONFIRMED booking as overdue. Only ACTIVE bookings can be overdue.");
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CONFIRMED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus target) {
        return getAllowedTransitions().contains(target);
    }

    @Override
    public Set<BookingStatus> getAllowedTransitions() {
        return Set.of(
                BookingStatus.ACTIVE,
                BookingStatus.CANCELLED
        );
    }

    @Override
    public boolean canEnter(BookingStateContext context) {
        // Can only enter confirmed state if start date is not in the past
        LocalDateTime startDate = context.getBooking().getStartDate();
        return startDate == null || !startDate.isBefore(LocalDateTime.now().minusDays(1));
    }

    @Override
    public void onEntry(BookingStateContext context) {
        log.info("Entered CONFIRMED state for booking {}", context.getBooking().getId());
        // Could send notification to borrower that booking is confirmed
    }

    @Override
    public void onExit(BookingStateContext context) {
        log.info("Exiting CONFIRMED state for booking {}", context.getBooking().getId());
    }
}