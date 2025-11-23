package com.locallend.locallend.event;

/**
 * Interface for event handlers that process specific domain events.
 * Implements the Observer pattern for event-driven architecture.
 *
 * @param <E> The type of event this handler processes
 */
public interface EventHandler<E extends DomainEvent> {

    /**
     * Handles the domain event.
     *
     * @param event The event to handle
     */
    void handle(E event);

    /**
     * Gets the type of event this handler can process.
     *
     * @return The event class type
     */
    Class<E> getEventType();

    /**
     * Gets the priority of this handler (lower values = higher priority).
     * Used to order handler execution when multiple handlers exist for the same event.
     *
     * @return The priority value
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Determines if this handler should process the event asynchronously.
     *
     * @return true if the handler should run asynchronously
     */
    default boolean isAsync() {
        return false;
    }
}