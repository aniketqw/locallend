package com.locallend.locallend.state.booking.states;

import com.locallend.locallend.exception.InvalidStateTransitionException;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.state.booking.BookingState;
import com.locallend.locallend.state.booking.BookingStateContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

/**
 * Cancelled state - terminal state when booking is cancelled.
 * No further transitions are allowed from this state.
 */
@Slf4j
public class CancelledState implements BookingState {

    @Override
    public void confirm(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot perform any transitions on a cancelled booking");
    }

    @Override
    public void activate(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot activate a cancelled booking");
    }

    @Override
    public void complete(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot complete a cancelled booking");
    }

    @Override
    public void cancel(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Booking is already cancelled");
    }

    @Override
    public void reject(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot reject a cancelled booking");
    }

    @Override
    public void markOverdue(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot mark a cancelled booking as overdue");
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CANCELLED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus target) {
        return false; // Terminal state - no transitions allowed
    }

    @Override
    public Set<BookingStatus> getAllowedTransitions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public void onEntry(BookingStateContext context) {
        log.info("Entered CANCELLED state for booking {}. Reason: {}",
                context.getBooking().getId(), context.getTransitionReason());

        // If item was marked as borrowed, make it available again
        if (context.getBooking().getItem() != null &&
            context.getBooking().getItem().getStatus() == com.locallend.locallend.model.enums.ItemStatus.BORROWED) {
            context.getBooking().getItem().setStatus(com.locallend.locallend.model.enums.ItemStatus.AVAILABLE);
            log.info("Item {} made available again after booking cancellation",
                    context.getBooking().getItemId());
        }
    }

    @Override
    public void onExit(BookingStateContext context) {
        // This should never be called as it's a terminal state
        log.warn("Attempting to exit CANCELLED state for booking {}. This should not happen.",
                context.getBooking().getId());
    }
}