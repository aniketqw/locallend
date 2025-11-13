package com.locallend.locallend.dto.response;

import com.locallend.locallend.model.enums.BookingStatus;
import java.time.LocalDateTime;

/**
 * DTO for returning complete booking information to API consumers.
 */
public class BookingResponseDto {

    private String id;
    private String itemId;
    private String itemName;
    private String itemImageUrl;
    private String borrowerId;
    private String borrowerName;
    private String ownerId;
    private String ownerName;
    private BookingStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private String bookingNotes;
    private String ownerNotes;
    private Double depositAmount;
    private Boolean depositPaid;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime confirmedDate;
    private LocalDateTime pickupDate;
    private LocalDateTime returnDate;
    private LocalDateTime cancelledDate;
    private String cancellationReason;
    private Boolean isRated;
    private Integer durationDays;

    // Computed fields
    private String statusDescription;
    private String timeAgo;
    private Long daysUntilStart;
    private Long daysUntilEnd;
    private Boolean canBeCancelled;
    private Boolean canBeConfirmed;
    private Boolean canBeActivated;
    private Boolean canBeCompleted;
    private Boolean isOverdue;
    private Boolean requiresDeposit;

    public BookingResponseDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemImageUrl() { return itemImageUrl; }
    public void setItemImageUrl(String itemImageUrl) { this.itemImageUrl = itemImageUrl; }
    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public LocalDateTime getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(LocalDateTime actualStartDate) { this.actualStartDate = actualStartDate; }
    public LocalDateTime getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDateTime actualEndDate) { this.actualEndDate = actualEndDate; }
    public String getBookingNotes() { return bookingNotes; }
    public void setBookingNotes(String bookingNotes) { this.bookingNotes = bookingNotes; }
    public String getOwnerNotes() { return ownerNotes; }
    public void setOwnerNotes(String ownerNotes) { this.ownerNotes = ownerNotes; }
    public Double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(Double depositAmount) { this.depositAmount = depositAmount; }
    public Boolean getDepositPaid() { return depositPaid; }
    public void setDepositPaid(Boolean depositPaid) { this.depositPaid = depositPaid; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public LocalDateTime getConfirmedDate() { return confirmedDate; }
    public void setConfirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; }
    public LocalDateTime getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDateTime pickupDate) { this.pickupDate = pickupDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public LocalDateTime getCancelledDate() { return cancelledDate; }
    public void setCancelledDate(LocalDateTime cancelledDate) { this.cancelledDate = cancelledDate; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public Boolean getIsRated() { return isRated; }
    public void setIsRated(Boolean isRated) { this.isRated = isRated; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public Long getDaysUntilStart() { return daysUntilStart; }
    public void setDaysUntilStart(Long daysUntilStart) { this.daysUntilStart = daysUntilStart; }
    public Long getDaysUntilEnd() { return daysUntilEnd; }
    public void setDaysUntilEnd(Long daysUntilEnd) { this.daysUntilEnd = daysUntilEnd; }
    public Boolean getCanBeCancelled() { return canBeCancelled; }
    public void setCanBeCancelled(Boolean canBeCancelled) { this.canBeCancelled = canBeCancelled; }
    public Boolean getCanBeConfirmed() { return canBeConfirmed; }
    public void setCanBeConfirmed(Boolean canBeConfirmed) { this.canBeConfirmed = canBeConfirmed; }
    public Boolean getCanBeActivated() { return canBeActivated; }
    public void setCanBeActivated(Boolean canBeActivated) { this.canBeActivated = canBeActivated; }
    public Boolean getCanBeCompleted() { return canBeCompleted; }
    public void setCanBeCompleted(Boolean canBeCompleted) { this.canBeCompleted = canBeCompleted; }
    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }
    public Boolean getRequiresDeposit() { return requiresDeposit; }
    public void setRequiresDeposit(Boolean requiresDeposit) { this.requiresDeposit = requiresDeposit; }

    @Override
    public String toString() {
        return "BookingResponseDto{" +
                "id='" + id + '\'' +
                ", itemId='" + itemId + '\'' +
                ", borrowerId='" + borrowerId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", durationDays=" + durationDays +
                '}';
    }
}
