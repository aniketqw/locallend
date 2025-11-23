package com.locallend.locallend.model;

import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.state.booking.BookingState;
import com.locallend.locallend.state.booking.BookingStateMachine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Booking entity representing a reservation/borrowing transaction in LocalLend platform.
 * Manages the complete lifecycle from request to completion.
 *
 * Enhanced with State Pattern support for better state management.
 */
@Document(collection = "bookings")
@CompoundIndex(def = "{'borrower_id': 1, 'status': 1}")
@CompoundIndex(def = "{'owner_id': 1, 'status': 1}")
@CompoundIndex(def = "{'item_id': 1, 'status': 1}")
@CompoundIndex(def = "{'status': 1, 'created_date': -1}")
@CompoundIndex(def = "{'start_date': 1, 'end_date': 1}")
@Builder(toBuilder = true)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
public class Booking {

    // State Pattern support - not persisted to database (excluded from builder)
    @Transient
    @Builder.Default
    private BookingState currentState = null;

    @Transient
    @Builder.Default
    private BookingStateMachine stateMachine = null;

    @Id
    private String id;

    @DBRef
    @NotNull(message = "Booking must have an item")
    private Item item;

    @Field("item_id")
    @Indexed
    @NotBlank(message = "Item ID is required")
    private String itemId;

    @DBRef
    @NotNull(message = "Booking must have a borrower")
    private User borrower;

    @Field("borrower_id")
    @Indexed
    @NotBlank(message = "Borrower ID is required")
    private String borrowerId;

    @DBRef
    @NotNull(message = "Booking must have an owner")
    private User owner;

    @Field("owner_id")
    @Indexed
    @NotBlank(message = "Owner ID is required")
    private String ownerId;

    @Indexed
    @NotNull(message = "Booking status is required")
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Field("start_date")
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @Field("end_date")
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @Field("actual_start_date")
    private LocalDateTime actualStartDate;

    @Field("actual_end_date")
    private LocalDateTime actualEndDate;

    @Size(max = 500, message = "Booking notes cannot exceed 500 characters")
    @Field("booking_notes")
    private String bookingNotes;

    @Size(max = 500, message = "Owner notes cannot exceed 500 characters")
    @Field("owner_notes")
    private String ownerNotes;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    @Field("deposit_amount")
    @Builder.Default
    private Double depositAmount = 0.0;

    @Field("deposit_paid")
    @Builder.Default
    private Boolean depositPaid = false;

    @Field("created_date")
    private LocalDateTime createdDate;

    @Field("updated_date")
    private LocalDateTime updatedDate;

    @Field("confirmed_date")
    private LocalDateTime confirmedDate;

    @Field("pickup_date")
    private LocalDateTime pickupDate;

    @Field("return_date")
    private LocalDateTime returnDate;

    @Field("cancelled_date")
    private LocalDateTime cancelledDate;

    @Field("cancellation_reason")
    @Size(max = 200, message = "Cancellation reason cannot exceed 200 characters")
    private String cancellationReason;

    @Field("is_rated")
    @Builder.Default
    private Boolean isRated = false;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    @Field("duration_days")
    private Integer durationDays;

    // Legacy state transition methods removed - now handled by State Pattern
    // See: BookingStateMachine and state implementations in state.booking.states package

    public boolean isOverdue() {
        if (this.status == BookingStatus.ACTIVE && this.endDate != null) {
            return LocalDateTime.now().isAfter(this.endDate);
        }
        return this.status == BookingStatus.OVERDUE;
    }

    public void calculateDuration() {
        if (this.startDate != null && this.endDate != null) {
            this.durationDays = (int) java.time.Duration.between(
                    this.startDate.toLocalDate().atStartOfDay(),
                    this.endDate.toLocalDate().atStartOfDay()
            ).toDays() + 1;
        }
    }

    public boolean requiresDeposit() {
        return this.depositAmount != null && this.depositAmount > 0;
    }

    public long getDaysUntilStart() {
        if (this.startDate != null) {
            return java.time.Duration.between(LocalDateTime.now(), this.startDate).toDays();
        }
        return 0;
    }

    public long getDaysUntilEnd() {
        if (this.endDate != null) {
            return java.time.Duration.between(LocalDateTime.now(), this.endDate).toDays();
        }
        return 0;
    }

    public void updateDates(LocalDateTime newStartDate, LocalDateTime newEndDate) {
        if (newStartDate.isAfter(newEndDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        this.startDate = newStartDate;
        this.endDate = newEndDate;
        this.calculateDuration();
        this.updatedDate = LocalDateTime.now();
    }

    // State Pattern Methods

    /**
     * Initializes the state machine for this booking.
     * Should be called after loading from database.
     *
     * @param eventPublisher The event publisher for state change events
     */
    public void initializeStateMachine(com.locallend.locallend.event.EventPublisher eventPublisher) {
        if (this.stateMachine == null) {
            this.stateMachine = new BookingStateMachine(this, eventPublisher);
            this.currentState = stateMachine.getCurrentState();
        }
    }

    /**
     * Gets the current state machine.
     * Initializes with null event publisher if not already initialized.
     *
     * @return The booking state machine
     */
    public BookingStateMachine getStateMachine() {
        if (this.stateMachine == null) {
            initializeStateMachine(null);
        }
        return this.stateMachine;
    }

    /**
     * Checks if the state machine is initialized.
     *
     * @return true if state machine is ready
     */
    public boolean hasStateMachine() {
        return this.stateMachine != null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Item getItem() { return item; }
    public void setItem(Item item) {
        this.item = item;
        this.itemId = item != null ? item.getId() : null;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public User getBorrower() { return borrower; }
    public void setBorrower(User borrower) {
        this.borrower = borrower;
        this.borrowerId = borrower != null ? borrower.getId() : null;
    }

    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) {
        this.owner = owner;
        this.ownerId = owner != null ? owner.getId() : null;
    }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{" +
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
