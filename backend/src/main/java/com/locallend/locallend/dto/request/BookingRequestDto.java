package com.locallend.locallend.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO for creating new bookings in the LocalLend platform.
 */
public class BookingRequestDto {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @Size(max = 500, message = "Booking notes cannot exceed 500 characters")
    private String bookingNotes;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    @DecimalMax(value = "10000.0", message = "Deposit amount cannot exceed 10,000")
    private Double depositAmount;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private Integer requestedDurationDays;

    private Boolean acceptTerms = false;

    public BookingRequestDto() {}

    public BookingRequestDto(String itemId, LocalDateTime startDate, LocalDateTime endDate) {
        this.itemId = itemId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @AssertTrue(message = "End date must be after start date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) return true;
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Must accept terms and conditions")
    public boolean isTermsAccepted() {
        return acceptTerms != null && acceptTerms;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getBookingNotes() { return bookingNotes; }
    public void setBookingNotes(String bookingNotes) { this.bookingNotes = bookingNotes; }
    public Double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(Double depositAmount) { this.depositAmount = depositAmount; }
    public Integer getRequestedDurationDays() { return requestedDurationDays; }
    public void setRequestedDurationDays(Integer requestedDurationDays) { this.requestedDurationDays = requestedDurationDays; }
    public Boolean getAcceptTerms() { return acceptTerms; }
    public void setAcceptTerms(Boolean acceptTerms) { this.acceptTerms = acceptTerms; }

    @Override
    public String toString() {
        return "BookingRequestDto{" +
                "itemId='" + itemId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", requestedDurationDays=" + requestedDurationDays +
                ", acceptTerms=" + acceptTerms +
                '}';
    }
}
