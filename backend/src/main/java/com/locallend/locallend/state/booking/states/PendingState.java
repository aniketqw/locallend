package com.locallend.locallend.state.booking.states;

import com.locallend.locallend.event.booking.BookingConfirmedEvent;
import com.locallend.locallend.exception.InvalidStateTransitionException;
import com.locallend.locallend.exception.UnauthorizedAccessException;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.state.booking.BookingState;
import com.locallend.locallend.state.booking.BookingStateContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Pending state - initial state when a booking is created.
 * From here, booking can be confirmed, cancelled, or rejected.
 */
@Slf4j
public class PendingState implements BookingState {

    @Override
    public void confirm(BookingStateContext context) {
        // Only owner can confirm
        if (!context.isOwner()) {
            throw new UnauthorizedAccessException("Only the owner can confirm the booking");
        }

        // Update booking data
        context.getBooking().setStatus(BookingStatus.CONFIRMED);
        context.getBooking().setConfirmedDate(LocalDateTime.now());

        String ownerNotes = context.getMetadata("ownerNotes", String.class);
        if (ownerNotes != null) {
            context.getBooking().setOwnerNotes(ownerNotes);
        }

        // Transition to confirmed state
        context.transitionTo(new ConfirmedState());

        // Publish event
        context.publishEvent(new BookingConfirmedEvent(
                context.getBooking().getId(),
                context.getBooking().getItemId(),
                context.getBooking().getBorrowerId(),
                context.getBooking().getOwnerId(),
                ownerNotes
        ));

        log.info("Booking {} confirmed by owner {}", context.getBooking().getId(), context.getUserId());
    }

    @Override
    public void activate(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot activate booking from PENDING state. Booking must be CONFIRMED first.");
    }

    @Override
    public void complete(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot complete booking from PENDING state.");
    }

    @Override
    public void cancel(BookingStateContext context) {
        // Both borrower and owner can cancel
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
        // Only owner can reject
        if (!context.isOwner()) {
            throw new UnauthorizedAccessException("Only the owner can reject the booking");
        }

        context.getBooking().setStatus(BookingStatus.REJECTED);
        context.transitionTo(new RejectedState());

        log.info("Booking {} rejected by owner {}", context.getBooking().getId(), context.getUserId());
    }

    @Override
    public void markOverdue(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot mark PENDING booking as overdue");
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.PENDING;
    }

    @Override
    public boolean canTransitionTo(BookingStatus target) {
        return getAllowedTransitions().contains(target);
    }

    @Override
    public Set<BookingStatus> getAllowedTransitions() {
        return Set.of(
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED,
                BookingStatus.REJECTED
        );
    }

    @Override
    public void onEntry(BookingStateContext context) {
        log.info("Entered PENDING state for booking {}", context.getBooking().getId());
    }

    @Override
    public void onExit(BookingStateContext context) {
        log.info("Exiting PENDING state for booking {}", context.getBooking().getId());
    }
}