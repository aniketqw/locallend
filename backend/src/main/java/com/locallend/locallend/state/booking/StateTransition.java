package com.locallend.locallend.state.booking;

import com.locallend.locallend.model.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Represents a state transition in the booking lifecycle.
 * Used for audit and tracking purposes.
 */
@Getter
@Builder
public class StateTransition {

    private final BookingStatus fromState;
    private final BookingStatus toState;
    private final String triggeredBy;
    private final String reason;
    private final LocalDateTime transitionTime;
    private final boolean successful;
    private final String errorMessage;

    /**
     * Creates a successful transition record.
     */
    public static StateTransition success(BookingStatus from, BookingStatus to,
                                         String triggeredBy, String reason) {
        return StateTransition.builder()
                .fromState(from)
                .toState(to)
                .triggeredBy(triggeredBy)
                .reason(reason)
                .transitionTime(LocalDateTime.now())
                .successful(true)
                .build();
    }

    /**
     * Creates a failed transition record.
     */
    public static StateTransition failure(BookingStatus from, BookingStatus to,
                                         String triggeredBy, String errorMessage) {
        return StateTransition.builder()
                .fromState(from)
                .toState(to)
                .triggeredBy(triggeredBy)
                .transitionTime(LocalDateTime.now())
                .successful(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Gets a human-readable description of the transition.
     */
    public String getDescription() {
        if (successful) {
            return String.format("Successfully transitioned from %s to %s: %s",
                    fromState, toState, reason);
        } else {
            return String.format("Failed to transition from %s to %s: %s",
                    fromState, toState, errorMessage);
        }
    }
}