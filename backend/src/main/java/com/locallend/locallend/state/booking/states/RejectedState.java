package com.locallend.locallend.state.booking.states;

import com.locallend.locallend.exception.InvalidStateTransitionException;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.state.booking.BookingState;
import com.locallend.locallend.state.booking.BookingStateContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

/**
 * Rejected state - terminal state when booking is rejected by owner.
 * No further transitions are allowed from this state.
 */
@Slf4j
public class RejectedState implements BookingState {

    @Override
    public void confirm(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot perform any transitions on a rejected booking");
    }

    @Override
    public void activate(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot activate a rejected booking");
    }

    @Override
    public void complete(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot complete a rejected booking");
    }

    @Override
    public void cancel(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot cancel a rejected booking");
    }

    @Override
    public void reject(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Booking is already rejected");
    }

    @Override
    public void markOverdue(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot mark a rejected booking as overdue");
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.REJECTED;
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
        log.info("Entered REJECTED state for booking {}. Reason: {}",
                context.getBooking().getId(), context.getTransitionReason());

        // Could trigger notification to borrower about rejection
        // Could suggest alternative dates if enabled
        Boolean suggestAlternatives = context.getMetadata("suggestAlternatives", Boolean.class);
        if (Boolean.TRUE.equals(suggestAlternatives)) {
            log.info("Suggesting alternative dates for rejected booking {}", context.getBooking().getId());
            // Logic to suggest alternatives would go here
        }
    }

    @Override
    public void onExit(BookingStateContext context) {
        // This should never be called as it's a terminal state
        log.warn("Attempting to exit REJECTED state for booking {}. This should not happen.",
                context.getBooking().getId());
    }
}