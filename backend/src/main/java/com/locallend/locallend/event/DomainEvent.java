package com.locallend.locallend.event;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events.
 * Domain events represent something that has happened in the system.
 */
public interface DomainEvent {

    /**
     * Gets the unique identifier of the event.
     *
     * @return The event ID
     */
    String getEventId();

    /**
     * Gets the type/name of the event.
     *
     * @return The event type
     */
    String getEventType();

    /**
     * Gets when the event occurred.
     *
     * @return The timestamp
     */
    LocalDateTime getOccurredAt();

    /**
     * Gets the aggregate ID this event relates to.
     *
     * @return The aggregate ID
     */
    String getAggregateId();

    /**
     * Gets the user who triggered the event.
     *
     * @return The user ID
     */
    String getUserId();
}