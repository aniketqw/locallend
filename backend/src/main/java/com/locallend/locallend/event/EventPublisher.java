package com.locallend.locallend.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for publishing domain events.
 * Uses Spring's ApplicationEventPublisher for event dispatching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    /**
     * Publishes a domain event.
     *
     * @param event The event to publish
     */
    public void publish(DomainEvent event) {
        if (event == null) {
            log.warn("Attempted to publish null event");
            return;
        }

        log.info("Publishing domain event: {} | Type: {} | AggregateId: {}",
                event.getEventId(), event.getEventType(), event.getAggregateId());

        try {
            // Publish using Spring's event system
            applicationEventPublisher.publishEvent(event);

            // Keep track of published events (for testing/auditing)
            publishedEvents.add(event);

            log.debug("Successfully published event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event: {} | Error: {}",
                    event.getEventId(), e.getMessage(), e);
            // Don't throw - event publishing should not break the main flow
        }
    }

    /**
     * Publishes multiple events.
     *
     * @param events The events to publish
     */
    public void publishAll(List<? extends DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        log.info("Publishing {} domain events", events.size());
        events.forEach(this::publish);
    }

    /**
     * Gets the count of published events (mainly for testing).
     *
     * @return The number of events published
     */
    public int getPublishedEventCount() {
        return publishedEvents.size();
    }

    /**
     * Clears the published events list (mainly for testing).
     */
    public void clearPublishedEvents() {
        publishedEvents.clear();
    }

    /**
     * Gets all published events (mainly for testing).
     *
     * @return List of published events
     */
    public List<DomainEvent> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }
}