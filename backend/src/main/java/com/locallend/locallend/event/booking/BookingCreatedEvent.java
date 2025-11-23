package com.locallend.locallend.event.booking;

import com.locallend.locallend.event.DomainEvent;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new booking is created.
 * This event triggers notifications, analytics, and other side effects.
 */
@Getter
@ToString
public class BookingCreatedEvent implements DomainEvent {

    private final String eventId;
    private final String eventType = "BookingCreated";
    private final LocalDateTime occurredAt;
    private final String aggregateId; // The booking ID
    private final String userId; // The user who created the booking

    // Booking specific data
    private final String bookingId;
    private final String itemId;
    private final String borrowerId;
    private final String ownerId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public BookingCreatedEvent(String bookingId, String itemId, String borrowerId,
                               String ownerId, LocalDateTime startDate, LocalDateTime endDate) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.aggregateId = bookingId;
        this.userId = borrowerId; // The borrower initiated the booking
        this.bookingId = bookingId;
        this.itemId = itemId;
        this.borrowerId = borrowerId;
        this.ownerId = ownerId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}