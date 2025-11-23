package com.locallend.locallend.command.booking;

import com.locallend.locallend.command.core.Command;
import com.locallend.locallend.dto.response.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Command to cancel a booking.
 * Can be used by either the borrower or owner to cancel a pending or confirmed booking.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelBookingCommand implements Command<BookingResponseDto> {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "User ID is required")
    private String userId;

    /**
     * Reason for cancellation
     */
    @NotNull(message = "Cancellation reason is required")
    private String cancellationReason;

    /**
     * Whether to notify the other party
     */
    @Builder.Default
    private boolean notifyOtherParty = true;

    /**
     * Whether this is a system-initiated cancellation
     */
    @Builder.Default
    private boolean systemCancellation = false;
}