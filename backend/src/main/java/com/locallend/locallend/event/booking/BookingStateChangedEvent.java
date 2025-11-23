package com.locallend.locallend.event.booking;

import com.locallend.locallend.event.DomainEvent;
import com.locallend.locallend.model.enums.BookingStatus;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generic event for any booking state change.
 * Used for audit logging and state tracking.
 */
@Getter
@ToString
public class BookingStateChangedEvent implements DomainEvent {

    private final String eventId;
    private final String eventType = "BookingStateChanged";
    private final LocalDateTime occurredAt;
    private final String aggregateId; // The booking ID
    private final String userId; // The user who triggered the change

    // State change data
    private final String bookingId;
    private final BookingStatus previousStatus;
    private final BookingStatus newStatus;
    private final String reason;
    private final LocalDateTime transitionTime;

    public BookingStateChangedEvent(String bookingId, String userId,
                                   BookingStatus previousStatus, BookingStatus newStatus,
                                   String reason) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.aggregateId = bookingId;
        this.userId = userId;
        this.bookingId = bookingId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.transitionTime = LocalDateTime.now();
    }

    /**
     * Checks if this is a terminal state transition.
     */
    public boolean isTerminalTransition() {
        return newStatus.isFinalStatus();
    }

    /**
     * Gets a human-readable description of the transition.
     */
    public String getTransitionDescription() {
        return String.format("Booking %s transitioned from %s to %s",
                bookingId, previousStatus, newStatus);
    }
}