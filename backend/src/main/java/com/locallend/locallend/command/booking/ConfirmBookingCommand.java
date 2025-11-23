package com.locallend.locallend.command.booking;

import com.locallend.locallend.command.core.Command;
import com.locallend.locallend.dto.response.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Command to confirm/approve a pending booking.
 * Used by item owners to approve booking requests from borrowers.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmBookingCommand implements Command<BookingResponseDto> {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Owner ID is required")
    private String ownerId;

    /**
     * Optional notes from the owner when confirming
     */
    private String ownerNotes;

    /**
     * Whether to send notification to borrower
     */
    @Builder.Default
    private boolean notifyBorrower = true;
}