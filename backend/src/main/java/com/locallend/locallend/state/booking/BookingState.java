package com.locallend.locallend.state.booking;

import com.locallend.locallend.model.enums.BookingStatus;

import java.util.Set;

/**
 * Interface for booking states in the State Pattern.
 * Each state defines allowed transitions and behaviors.
 */
public interface BookingState {

    /**
     * Confirms the booking (owner approval).
     *
     * @param context The state context
     */
    void confirm(BookingStateContext context);

    /**
     * Activates the booking (item pickup).
     *
     * @param context The state context
     */
    void activate(BookingStateContext context);

    /**
     * Completes the booking (item return).
     *
     * @param context The state context
     */
    void complete(BookingStateContext context);

    /**
     * Cancels the booking.
     *
     * @param context The state context
     */
    void cancel(BookingStateContext context);

    /**
     * Rejects the booking (owner rejection).
     *
     * @param context The state context
     */
    void reject(BookingStateContext context);

    /**
     * Marks the booking as overdue.
     *
     * @param context The state context
     */
    void markOverdue(BookingStateContext context);

    /**
     * Gets the booking status this state represents.
     *
     * @return The booking status
     */
    BookingStatus getStatus();

    /**
     * Checks if transition to target status is allowed from this state.
     *
     * @param target The target status
     * @return true if transition is allowed
     */
    boolean canTransitionTo(BookingStatus target);

    /**
     * Gets all allowed transitions from this state.
     *
     * @return Set of allowed target statuses
     */
    Set<BookingStatus> getAllowedTransitions();

    /**
     * Gets the name of this state.
     *
     * @return State name
     */
    default String getStateName() {
        return getStatus().name();
    }

    /**
     * Checks if this is a terminal state (no further transitions).
     *
     * @return true if terminal state
     */
    default boolean isTerminal() {
        return getStatus().isFinalStatus();
    }

    /**
     * Validates if the state can be entered.
     * Can be used to enforce business rules before state entry.
     *
     * @param context The state context
     * @return true if state entry is valid
     */
    default boolean canEnter(BookingStateContext context) {
        return true;
    }

    /**
     * Called when entering this state.
     * Can be used for side effects like notifications.
     *
     * @param context The state context
     */
    default void onEntry(BookingStateContext context) {
        // Override in concrete states if needed
    }

    /**
     * Called when exiting this state.
     *
     * @param context The state context
     */
    default void onExit(BookingStateContext context) {
        // Override in concrete states if needed
    }
}