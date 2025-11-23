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
 * Active state - borrower has picked up the item.
 * From here, booking can be completed or marked as overdue.
 */
@Slf4j
public class ActiveState implements BookingState {

    @Override
    public void confirm(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot confirm an already active booking");
    }

    @Override
    public void activate(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Booking is already active");
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

        // Check if booking was overdue
        boolean wasOverdue = context.getBooking().isOverdue();

        // Mark item as available again
        if (context.getBooking().getItem() != null) {
            context.getBooking().getItem().setStatus(ItemStatus.AVAILABLE);
        }

        // Transition to completed state
        context.transitionTo(new CompletedState());

        // Publish completion event
        context.publishEvent(new BookingCompletedEvent(
                context.getBooking().getId(),
                context.getBooking().getItemId(),
                context.getBooking().getBorrowerId(),
                context.getBooking().getOwnerId(),
                context.getBooking().getActualStartDate(),
                context.getBooking().getActualEndDate(),
                wasOverdue,
                returnCondition
        ));

        log.info("Booking {} completed by borrower {}", context.getBooking().getId(), context.getUserId());
    }

    @Override
    public void cancel(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot cancel an active booking. The item must be returned first.");
    }

    @Override
    public void reject(BookingStateContext context) {
        throw new InvalidStateTransitionException(
                "Cannot reject an active booking");
    }

    @Override
    public void markOverdue(BookingStateContext context) {
        // Only system can mark as overdue
        if (!context.isSystemTransition()) {
            throw new UnauthorizedAccessException("Only system can mark booking as overdue");
        }

        context.getBooking().setStatus(BookingStatus.OVERDUE);
        context.transitionTo(new OverdueState());

        log.warn("Booking {} marked as overdue", context.getBooking().getId());
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.ACTIVE;
    }

    @Override
    public boolean canTransitionTo(BookingStatus target) {
        return getAllowedTransitions().contains(target);
    }

    @Override
    public Set<BookingStatus> getAllowedTransitions() {
        return Set.of(
                BookingStatus.COMPLETED,
                BookingStatus.OVERDUE
        );
    }

    @Override
    public void onEntry(BookingStateContext context) {
        log.info("Entered ACTIVE state for booking {}", context.getBooking().getId());
        // Item is now in borrower's possession
    }

    @Override
    public void onExit(BookingStateContext context) {
        log.info("Exiting ACTIVE state for booking {}", context.getBooking().getId());
    }
}