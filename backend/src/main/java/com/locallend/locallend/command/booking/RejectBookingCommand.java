package com.locallend.locallend.command.booking;

import com.locallend.locallend.command.core.Command;
import com.locallend.locallend.dto.response.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Command to reject a pending booking.
 * Used by item owners to reject booking requests from borrowers.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectBookingCommand implements Command<BookingResponseDto> {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Owner ID is required")
    private String ownerId;

    /**
     * Reason for rejection
     */
    @NotNull(message = "Rejection reason is required")
    private String rejectionReason;

    /**
     * Whether to notify the borrower
     */
    @Builder.Default
    private boolean notifyBorrower = true;

    /**
     * Whether to suggest alternative dates
     */
    @Builder.Default
    private boolean suggestAlternatives = false;
}