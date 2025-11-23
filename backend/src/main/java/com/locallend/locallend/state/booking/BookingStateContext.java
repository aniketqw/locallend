package com.locallend.locallend.state.booking;

import com.locallend.locallend.event.DomainEvent;
import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.model.Booking;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Context for state transitions in the State Pattern.
 * Holds the booking being transitioned and related metadata.
 */
@Getter
@RequiredArgsConstructor
public class BookingStateContext {

    private final Booking booking;
    private final BookingStateMachine stateMachine;
    private final EventPublisher eventPublisher;

    // Metadata for the transition
    private final Map<String, Object> metadata = new HashMap<>();

    // Transition information
    private String transitionReason;
    private String userId;
    private LocalDateTime transitionTime;

    /**
     * Sets the user performing the transition.
     */
    public BookingStateContext withUser(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Sets the reason for the transition.
     */
    public BookingStateContext withReason(String reason) {
        this.transitionReason = reason;
        return this;
    }

    /**
     * Adds metadata to the context.
     */
    public BookingStateContext withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Gets metadata value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Transitions to a new state.
     */
    public void transitionTo(BookingState newState) {
        this.transitionTime = LocalDateTime.now();
        stateMachine.transitionTo(newState, this);
    }

    /**
     * Publishes a domain event.
     */
    public void publishEvent(DomainEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publish(event);
        }
    }

    /**
     * Updates the booking timestamp.
     */
    public void updateTimestamp() {
        booking.setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Checks if the current user is the borrower.
     */
    public boolean isBorrower() {
        return userId != null && userId.equals(booking.getBorrowerId());
    }

    /**
     * Checks if the current user is the owner.
     */
    public boolean isOwner() {
        return userId != null && userId.equals(booking.getOwnerId());
    }

    /**
     * Checks if this is a system-initiated transition.
     */
    public boolean isSystemTransition() {
        return "SYSTEM".equals(userId);
    }
}