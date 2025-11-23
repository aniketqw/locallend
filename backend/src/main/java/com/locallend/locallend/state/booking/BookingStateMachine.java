package com.locallend.locallend.state.booking;

import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.event.booking.BookingStateChangedEvent;
import com.locallend.locallend.exception.InvalidStateTransitionException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.state.booking.states.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * State machine for managing booking lifecycle.
 * Coordinates state transitions and enforces business rules.
 */
@Slf4j
@Getter
public class BookingStateMachine {

    private final Booking booking;
    private final EventPublisher eventPublisher;
    private BookingState currentState;

    // Registry of all possible states
    private final Map<BookingStatus, BookingState> stateRegistry;

    /**
     * Creates a new state machine for the booking.
     */
    public BookingStateMachine(Booking booking, EventPublisher eventPublisher) {
        this.booking = booking;
        this.eventPublisher = eventPublisher;
        this.stateRegistry = initializeStates();
        this.currentState = determineCurrentState();
    }

    /**
     * Initializes all possible states.
     */
    private Map<BookingStatus, BookingState> initializeStates() {
        Map<BookingStatus, BookingState> states = new HashMap<>();
        states.put(BookingStatus.PENDING, new PendingState());
        states.put(BookingStatus.CONFIRMED, new ConfirmedState());
        states.put(BookingStatus.ACTIVE, new ActiveState());
        states.put(BookingStatus.COMPLETED, new CompletedState());
        states.put(BookingStatus.CANCELLED, new CancelledState());
        states.put(BookingStatus.REJECTED, new RejectedState());
        states.put(BookingStatus.OVERDUE, new OverdueState());
        return states;
    }

    /**
     * Determines the current state based on booking status.
     */
    private BookingState determineCurrentState() {
        BookingStatus status = booking.getStatus();
        BookingState state = stateRegistry.get(status);
        if (state == null) {
            log.warn("Unknown booking status: {}, defaulting to PENDING", status);
            return stateRegistry.get(BookingStatus.PENDING);
        }
        return state;
    }

    /**
     * Confirms the booking.
     */
    public void confirm(String userId, String notes) {
        BookingStateContext context = createContext(userId, "Booking confirmed by owner");
        context.withMetadata("ownerNotes", notes);
        currentState.confirm(context);
    }

    /**
     * Activates the booking.
     */
    public void activate(String userId) {
        BookingStateContext context = createContext(userId, "Booking activated - item picked up");
        currentState.activate(context);
    }

    /**
     * Completes the booking.
     */
    public void complete(String userId, String returnCondition) {
        BookingStateContext context = createContext(userId, "Booking completed - item returned");
        context.withMetadata("returnCondition", returnCondition);
        currentState.complete(context);
    }

    /**
     * Cancels the booking.
     */
    public void cancel(String userId, String reason) {
        BookingStateContext context = createContext(userId, reason);
        currentState.cancel(context);
    }

    /**
     * Rejects the booking.
     */
    public void reject(String userId, String reason) {
        BookingStateContext context = createContext(userId, reason);
        currentState.reject(context);
    }

    /**
     * Marks the booking as overdue.
     */
    public void markOverdue() {
        BookingStateContext context = createContext("SYSTEM", "Booking marked as overdue");
        currentState.markOverdue(context);
    }

    /**
     * Transitions to a new state.
     */
    public void transitionTo(BookingState newState, BookingStateContext context) {
        BookingStatus oldStatus = currentState.getStatus();
        BookingStatus newStatus = newState.getStatus();

        // Validate transition
        if (!currentState.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(
                String.format("Invalid transition from %s to %s", oldStatus, newStatus)
            );
        }

        // Check if new state can be entered
        if (!newState.canEnter(context)) {
            throw new InvalidStateTransitionException(
                String.format("Cannot enter state %s due to business rules", newStatus)
            );
        }

        log.info("Transitioning booking {} from {} to {}",
            booking.getId(), oldStatus, newStatus);

        // Exit current state
        currentState.onExit(context);

        // Update booking status
        booking.setStatus(newStatus);
        context.updateTimestamp();

        // Enter new state
        currentState = newState;
        currentState.onEntry(context);

        // Publish state change event
        publishStateChangeEvent(oldStatus, newStatus, context);
    }

    /**
     * Creates a state context.
     */
    private BookingStateContext createContext(String userId, String reason) {
        return new BookingStateContext(booking, this, eventPublisher)
                .withUser(userId)
                .withReason(reason);
    }

    /**
     * Publishes a state change event.
     */
    private void publishStateChangeEvent(BookingStatus oldStatus, BookingStatus newStatus,
                                        BookingStateContext context) {
        if (eventPublisher != null) {
            BookingStateChangedEvent event = new BookingStateChangedEvent(
                booking.getId(),
                context.getUserId(),
                oldStatus,
                newStatus,
                context.getTransitionReason()
            );
            eventPublisher.publish(event);
        }
    }

    /**
     * Gets the current status.
     */
    public BookingStatus getCurrentStatus() {
        return currentState.getStatus();
    }

    /**
     * Checks if the booking is in a terminal state.
     */
    public boolean isTerminal() {
        return currentState.isTerminal();
    }

    /**
     * Checks if a transition to the target status is allowed.
     */
    public boolean canTransitionTo(BookingStatus target) {
        return currentState.canTransitionTo(target);
    }
}