package com.locallend.locallend.command.booking;

import com.locallend.locallend.command.core.Command;
import com.locallend.locallend.dto.response.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command to create a new booking.
 * Encapsulates all data needed to create a booking between a borrower and item owner.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingCommand implements Command<BookingResponseDto> {

    @NotNull(message = "Item ID is required")
    private String itemId;

    @NotNull(message = "Borrower ID is required")
    private String borrowerId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    private String bookingNotes;

    private BigDecimal depositAmount;

    /**
     * Validates that the date range is valid.
     *
     * @return true if end date is after start date
     */
    public boolean isValidDateRange() {
        return endDate != null && startDate != null && endDate.isAfter(startDate);
    }

    /**
     * Calculates the duration of the booking in days.
     *
     * @return The number of days between start and end date
     */
    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.Duration.between(startDate, endDate).toDays();
    }
}