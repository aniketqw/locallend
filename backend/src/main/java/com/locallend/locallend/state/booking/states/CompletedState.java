package com.locallend.locallend.state.booking.states;

import com.locallend.locallend.exception.InvalidStateTransitionException;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.state.booking.BookingState;
import com.locallend.locallend.state.booking.BookingStateContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

/**
 * Completed state - terminal state when booking is successfully completed.
 * No further transitions are allowed from this state.
 */
@Slf4j
public class CompletedState implements BookingState {

    @Override
    public void confirm(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot perform any transitions on a completed booking");
    }

    @Override
    public void activate(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot perform any transitions on a completed booking");
    }

    @Override
    public void complete(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Booking is already completed");
    }

    @Override
    public void cancel(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot cancel a completed booking");
    }

    @Override
    public void reject(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot reject a completed booking");
    }

    @Override
    public void markOverdue(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot mark a completed booking as overdue");
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.COMPLETED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus target) {
        return false; // Terminal state - no transitions allowed
    }

    @Override
    public Set<BookingStatus> getAllowedTransitions() {
        return Collections.emptySet(); // No transitions from completed state
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public void onEntry(BookingStateContext context) {
        log.info("Entered COMPLETED state for booking {}. This is a terminal state.",
                context.getBooking().getId());

        // Enable ratings for both parties
        context.getBooking().setIsRated(false); // Can now be rated

        // Calculate final metrics
        if (context.getBooking().getActualStartDate() != null &&
            context.getBooking().getActualEndDate() != null) {
            long actualDuration = java.time.Duration.between(
                    context.getBooking().getActualStartDate(),
                    context.getBooking().getActualEndDate()
            ).toDays();
            log.info("Booking {} completed. Actual duration: {} days",
                    context.getBooking().getId(), actualDuration);
        }
    }

    @Override
    public void onExit(BookingStateContext context) {
        // This should never be called as it's a terminal state
        log.warn("Attempting to exit COMPLETED state for booking {}. This should not happen.",
                context.getBooking().getId());
    }
}