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
 * Command to activate a confirmed booking.
 * Used when the borrower actually picks up the item and starts the borrowing period.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateBookingCommand implements Command<BookingResponseDto> {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Borrower ID is required")
    private String borrowerId;

    /**
     * The actual pickup date/time (defaults to now if not specified)
     */
    private LocalDateTime actualStartDate;

    /**
     * Any notes about the pickup condition
     */
    private String pickupNotes;

    /**
     * Whether the deposit was paid
     */
    @Builder.Default
    private boolean depositPaid = true;

    /**
     * Gets the actual start date, defaulting to now if not specified.
     *
     * @return The actual start date
     */
    public LocalDateTime getActualStartDate() {
        return actualStartDate != null ? actualStartDate : LocalDateTime.now();
    }
}