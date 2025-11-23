package com.locallend.locallend.event.booking;

import com.locallend.locallend.event.DomainEvent;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a booking is completed (item returned).
 * This event triggers trust score updates, notifications, and enables ratings.
 */
@Getter
@ToString
public class BookingCompletedEvent implements DomainEvent {

    private final String eventId;
    private final String eventType = "BookingCompleted";
    private final LocalDateTime occurredAt;
    private final String aggregateId; // The booking ID
    private final String userId; // The borrower who completed

    // Booking specific data
    private final String bookingId;
    private final String itemId;
    private final String borrowerId;
    private final String ownerId;
    private final LocalDateTime completedDate;
    private final LocalDateTime actualStartDate;
    private final LocalDateTime actualEndDate;
    private final boolean wasOverdue;
    private final String returnCondition;

    public BookingCompletedEvent(String bookingId, String itemId, String borrowerId,
                                 String ownerId, LocalDateTime actualStartDate,
                                 LocalDateTime actualEndDate, boolean wasOverdue,
                                 String returnCondition) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.aggregateId = bookingId;
        this.userId = borrowerId; // The borrower completed the booking
        this.bookingId = bookingId;
        this.itemId = itemId;
        this.borrowerId = borrowerId;
        this.ownerId = ownerId;
        this.completedDate = LocalDateTime.now();
        this.actualStartDate = actualStartDate;
        this.actualEndDate = actualEndDate;
        this.wasOverdue = wasOverdue;
        this.returnCondition = returnCondition;
    }
}