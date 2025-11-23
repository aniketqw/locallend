package com.locallend.locallend.event.booking;

import com.locallend.locallend.event.DomainEvent;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a booking is confirmed by the owner.
 */
@Getter
@ToString
public class BookingConfirmedEvent implements DomainEvent {

    private final String eventId;
    private final String eventType = "BookingConfirmed";
    private final LocalDateTime occurredAt;
    private final String aggregateId; // The booking ID
    private final String userId; // The owner who confirmed

    // Booking specific data
    private final String bookingId;
    private final String itemId;
    private final String borrowerId;
    private final String ownerId;
    private final LocalDateTime confirmedDate;
    private final String ownerNotes;

    public BookingConfirmedEvent(String bookingId, String itemId, String borrowerId,
                                 String ownerId, String ownerNotes) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.aggregateId = bookingId;
        this.userId = ownerId; // The owner confirmed the booking
        this.bookingId = bookingId;
        this.itemId = itemId;
        this.borrowerId = borrowerId;
        this.ownerId = ownerId;
        this.confirmedDate = LocalDateTime.now();
        this.ownerNotes = ownerNotes;
    }
}