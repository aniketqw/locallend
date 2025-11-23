package com.locallend.locallend.state.booking.states;

import com.locallend.locallend.event.booking.BookingCompletedEvent;
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
 * Overdue state - booking has passed its return date without completion.
 * Can still be completed (with penalties) from this state.
 */
@Slf4j
public class OverdueState implements BookingState {

    @Override
    public void confirm(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot confirm an overdue booking");
    }

    @Override
    public void activate(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot activate an overdue booking");
    }

    @Override
    public void complete(BookingStateContext context) {
        // Only borrower can complete (return the item)
        if (!context.isBorrower()) {
            throw new UnauthorizedAccessException("Only the borrower can complete the booking");
        }

        // Update booking data
        context.getBooking().setStatus(BookingStatus.COMPLETED);
        context.getBooking().setReturnDate(LocalDateTime.now());

        LocalDateTime actualEndDate = context.getMetadata("actualEndDate", LocalDateTime.class);
        if (actualEndDate != null) {
            context.getBooking().setActualEndDate(actualEndDate);
        } else {
            context.getBooking().setActualEndDate(LocalDateTime.now());
        }

        String returnCondition = context.getMetadata("returnCondition", String.class);

        // Mark item as available again
        if (context.getBooking().getItem() != null) {
            context.getBooking().getItem().setStatus(ItemStatus.AVAILABLE);
        }

        // Transition to completed state
        context.transitionTo(new CompletedState());

        // Publish completion event (with overdue flag set to true)
        context.publishEvent(new BookingCompletedEvent(
                context.getBooking().getId(),
                context.getBooking().getItemId(),
                context.getBooking().getBorrowerId(),
                context.getBooking().getOwnerId(),
                context.getBooking().getActualStartDate(),
                context.getBooking().getActualEndDate(),
                true, // was overdue
                returnCondition
        ));

        log.warn("Overdue booking {} completed by borrower {}. Late return will affect trust score.",
                context.getBooking().getId(), context.getUserId());
    }

    @Override
    public void cancel(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot cancel an overdue booking. The item must be returned.");
    }

    @Override
    public void reject(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot reject an overdue booking");
    }

    @Override
    public void markOverdue(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Booking is already marked as overdue");
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.OVERDUE;
    }

    @Override
    public boolean canTransitionTo(BookingStatus target) {
        return getAllowedTransitions().contains(target);
    }

    @Override
    public Set<BookingStatus> getAllowedTransitions() {
        // Can only complete from overdue state
        return Set.of(BookingStatus.COMPLETED);
    }

    @Override
    public void onEntry(BookingStateContext context) {
        log.warn("Entered OVERDUE state for booking {}. This will negatively impact borrower's trust score.",
                context.getBooking().getId());

        // Calculate days overdue
        if (context.getBooking().getEndDate() != null) {
            long daysOverdue = java.time.Duration.between(
                    context.getBooking().getEndDate(),
                    LocalDateTime.now()
            ).toDays();
            log.warn("Booking {} is {} days overdue", context.getBooking().getId(), daysOverdue);
        }

        // Could trigger penalty calculation
        // Could send urgent notification to borrower
        // Could notify owner about overdue status
    }

    @Override
    public void onExit(BookingStateContext context) {
        log.info("Exiting OVERDUE state for booking {}", context.getBooking().getId());
    }
}