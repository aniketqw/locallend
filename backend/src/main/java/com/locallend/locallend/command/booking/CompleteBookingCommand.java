package com.locallend.locallend.command.booking;

import com.locallend.locallend.command.core.Command;
import com.locallend.locallend.dto.response.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Command to complete an active booking.
 * Used when the borrower returns the item to the owner.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteBookingCommand implements Command<BookingResponseDto> {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Borrower ID is required")
    private String borrowerId;

    /**
     * The actual return date/time (defaults to now if not specified)
     */
    private LocalDateTime actualEndDate;

    /**
     * Any notes about the return condition
     */
    private String returnNotes;

    /**
     * Whether the deposit should be refunded
     */
    @Builder.Default
    private boolean refundDeposit = true;

    /**
     * Item condition upon return (for tracking damage)
     */
    private String returnCondition;

    /**
     * Gets the actual end date, defaulting to now if not specified.
     *
     * @return The actual end date
     */
    public LocalDateTime getActualEndDate() {
        return actualEndDate != null ? actualEndDate : LocalDateTime.now();
    }
}