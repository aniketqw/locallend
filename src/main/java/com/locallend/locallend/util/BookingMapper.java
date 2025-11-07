package com.locallend.locallend.util;

import com.locallend.locallend.model.Booking;
import com.locallend.locallend.dto.response.BookingResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between Booking entities and DTOs.
 */
@Component
public class BookingMapper {

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) return null;

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setItemId(booking.getItemId());
        dto.setBorrowerId(booking.getBorrowerId());
        dto.setOwnerId(booking.getOwnerId());
        dto.setStatus(booking.getStatus());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setActualStartDate(booking.getActualStartDate());
        dto.setActualEndDate(booking.getActualEndDate());
        dto.setBookingNotes(booking.getBookingNotes());
        dto.setOwnerNotes(booking.getOwnerNotes());
        dto.setDepositAmount(booking.getDepositAmount());
        dto.setDepositPaid(booking.getDepositPaid());
        dto.setCreatedDate(booking.getCreatedDate());
        dto.setUpdatedDate(booking.getUpdatedDate());
        dto.setConfirmedDate(booking.getConfirmedDate());
        dto.setPickupDate(booking.getPickupDate());
        dto.setReturnDate(booking.getReturnDate());
        dto.setCancelledDate(booking.getCancelledDate());
        dto.setCancellationReason(booking.getCancellationReason());
        dto.setIsRated(booking.getIsRated());
        dto.setDurationDays(booking.getDurationDays());

        if (booking.getItem() != null) {
            dto.setItemName(booking.getItem().getName());
            if (booking.getItem().getImages() != null && !booking.getItem().getImages().isEmpty()) {
                dto.setItemImageUrl(booking.getItem().getImages().get(0));
            }
        }
        if (booking.getBorrower() != null) {
            dto.setBorrowerName(booking.getBorrower().getName());
        }
        if (booking.getOwner() != null) {
            dto.setOwnerName(booking.getOwner().getName());
        }

        dto.setStatusDescription(booking.getStatus() != null ? booking.getStatus().getDescription() : null);
        dto.setTimeAgo(calculateTimeAgo(booking.getCreatedDate()));
        dto.setDaysUntilStart(booking.getDaysUntilStart());
        dto.setDaysUntilEnd(booking.getDaysUntilEnd());
        dto.setCanBeCancelled(booking.getStatus() != null && booking.getStatus().canBeCancelled());
        dto.setCanBeConfirmed(booking.getStatus() != null && booking.getStatus().canBeConfirmed());
        dto.setCanBeActivated(booking.getStatus() != null && booking.getStatus().canBeActivated());
        dto.setCanBeCompleted(booking.getStatus() != null && booking.getStatus().canBeCompleted());
        dto.setIsOverdue(booking.isOverdue());
        dto.setRequiresDeposit(booking.requiresDeposit());

        return dto;
    }

    public List<BookingResponseDto> toBookingResponseDtoList(List<Booking> bookings) {
        if (bookings == null) return null;
        return bookings.stream().map(this::toBookingResponseDto).collect(Collectors.toList());
    }

    public BookingResponseDto toBookingSummaryDto(Booking booking) {
        if (booking == null) return null;
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setItemId(booking.getItemId());
        dto.setBorrowerId(booking.getBorrowerId());
        dto.setOwnerId(booking.getOwnerId());
        dto.setStatus(booking.getStatus());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setDepositAmount(booking.getDepositAmount());
        dto.setCreatedDate(booking.getCreatedDate());
        dto.setDurationDays(booking.getDurationDays());
        if (booking.getItem() != null) dto.setItemName(booking.getItem().getName());
        if (booking.getBorrower() != null) dto.setBorrowerName(booking.getBorrower().getName());
        if (booking.getOwner() != null) dto.setOwnerName(booking.getOwner().getName());
        dto.setStatusDescription(booking.getStatus() != null ? booking.getStatus().getDescription() : null);
        dto.setTimeAgo(calculateTimeAgo(booking.getCreatedDate()));
        dto.setIsOverdue(booking.isOverdue());
        return dto;
    }

    public List<BookingResponseDto> toBookingSummaryDtoList(List<Booking> bookings) {
        if (bookings == null) return null;
        return bookings.stream().map(this::toBookingSummaryDto).collect(Collectors.toList());
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        if (days < 7) return days + " day" + (days == 1 ? "" : "s") + " ago";
        if (days < 30) return (days / 7) + " week" + ((days / 7) == 1 ? "" : "s") + " ago";
        if (days < 365) return (days / 30) + " month" + ((days / 30) == 1 ? "" : "s") + " ago";
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}
