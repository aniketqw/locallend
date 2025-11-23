package com.locallend.locallend.controller;

import com.locallend.locallend.command.booking.*;
import com.locallend.locallend.command.core.CommandExecutor;
import com.locallend.locallend.command.core.CommandResult;
import com.locallend.locallend.dto.request.BookingRequestDto;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.service.booking.BookingQueryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Booking Controller - Refactored to use Command Pattern
 *
 * All write operations use Command Pattern for better separation of concerns.
 * All read operations use BookingQueryService (CQRS pattern).
 */
@Slf4j
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    private final CommandExecutor commandExecutor;
    private final BookingQueryService queryService;

    @Autowired
    public BookingController(CommandExecutor commandExecutor,
                           BookingQueryService queryService) {
        this.commandExecutor = commandExecutor;
        this.queryService = queryService;
    }

    /**
     * Create a new booking request
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequestDto request,
                                           @RequestHeader("X-User-Id") String borrowerId) {
        try {
            CreateBookingCommand command = CreateBookingCommand.builder()
                    .itemId(request.getItemId())
                    .borrowerId(borrowerId)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .bookingNotes(request.getBookingNotes())
                    .depositAmount(request.getDepositAmount() != null ?
                            java.math.BigDecimal.valueOf(request.getDepositAmount()) : null)
                    .build();

            CommandResult<BookingResponseDto> result = commandExecutor.execute(command, borrowerId);

            if (!result.isSuccess()) {
                return error(HttpStatus.BAD_REQUEST, "Booking creation failed",
                        result.getErrorMessage(), result.getErrorCode());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
        } catch (Exception e) {
            log.error("Unexpected error creating booking", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error",
                    e.getMessage(), "INTERNAL_ERROR");
        }
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable String id) {
        try {
            BookingResponseDto dto = queryService.getBookingById(id);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        }
    }

    /**
     * Get borrower's bookings
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<?> myBookings(@RequestHeader("X-User-Id") String userId) {
        List<BookingResponseDto> list = queryService.getBookingsForBorrower(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get pending approvals for owner
     */
    @GetMapping("/pending-approvals")
    public ResponseEntity<?> pendingApprovals(@RequestHeader("X-User-Id") String ownerId) {
        try {
            com.locallend.locallend.model.enums.BookingStatus pendingStatus =
                com.locallend.locallend.model.enums.BookingStatus.PENDING;
            List<BookingResponseDto> bookings = queryService.getBookingsForOwner(ownerId);
            List<BookingResponseDto> pending = bookings.stream()
                .filter(b -> b.getStatus() == pendingStatus)
                .toList();
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching pending approvals",
                    e.getMessage(), "FETCH_ERROR");
        }
    }

    /**
     * Get owner's bookings
     */
    @GetMapping("/my-owned")
    public ResponseEntity<?> myOwnedBookings(@RequestHeader("X-User-Id") String ownerId) {
        List<BookingResponseDto> list = queryService.getBookingsForOwner(ownerId);
        return ResponseEntity.ok(list);
    }

    /**
     * Confirm booking (owner approves the booking request)
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable String id,
                                      @RequestHeader("X-User-Id") String ownerId,
                                      @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("ownerNotes") : null;
        try {
            ConfirmBookingCommand command = ConfirmBookingCommand.builder()
                    .bookingId(id)
                    .ownerId(ownerId)
                    .ownerNotes(notes)
                    .build();

            CommandResult<BookingResponseDto> result = commandExecutor.execute(command, ownerId);

            if (!result.isSuccess()) {
                return error(HttpStatus.BAD_REQUEST, "Cannot confirm booking",
                        result.getErrorMessage(), result.getErrorCode());
            }

            return ResponseEntity.ok(result.getData());
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (Exception e) {
            log.error("Error confirming booking", e);
            return error(HttpStatus.BAD_REQUEST, "Cannot confirm booking",
                    e.getMessage(), "BOOKING_CONFIRM_ERROR");
        }
    }

    /**
     * Activate booking (borrower picks up the item)
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable String id,
                                       @RequestHeader("X-User-Id") String borrowerId,
                                       @RequestBody(required = false) Map<String, Object> body) {
        try {
            LocalDateTime actualStartDate = body != null && body.containsKey("actualStartDate")
                    ? LocalDateTime.parse(body.get("actualStartDate").toString())
                    : LocalDateTime.now();
            String pickupNotes = body != null ? (String) body.get("pickupNotes") : null;
            Boolean depositPaid = body != null ? (Boolean) body.get("depositPaid") : true;

            ActivateBookingCommand command = ActivateBookingCommand.builder()
                    .bookingId(id)
                    .borrowerId(borrowerId)
                    .actualStartDate(actualStartDate)
                    .pickupNotes(pickupNotes)
                    .depositPaid(depositPaid)
                    .build();

            CommandResult<BookingResponseDto> result = commandExecutor.execute(command, borrowerId);

            if (!result.isSuccess()) {
                return error(HttpStatus.BAD_REQUEST, "Cannot activate booking",
                        result.getErrorMessage(), result.getErrorCode());
            }

            return ResponseEntity.ok(result.getData());
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (Exception e) {
            log.error("Error activating booking", e);
            return error(HttpStatus.BAD_REQUEST, "Cannot activate booking",
                    e.getMessage(), "BOOKING_ACTIVATE_ERROR");
        }
    }

    /**
     * Complete booking (borrower returns the item)
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable String id,
                                       @RequestHeader("X-User-Id") String borrowerId,
                                       @RequestBody(required = false) Map<String, Object> body) {
        try {
            LocalDateTime actualEndDate = body != null && body.containsKey("actualEndDate")
                    ? LocalDateTime.parse(body.get("actualEndDate").toString())
                    : LocalDateTime.now();
            String returnNotes = body != null ? (String) body.get("returnNotes") : null;
            String returnCondition = body != null ? (String) body.get("returnCondition") : "GOOD";

            CompleteBookingCommand command = CompleteBookingCommand.builder()
                    .bookingId(id)
                    .borrowerId(borrowerId)
                    .actualEndDate(actualEndDate)
                    .returnNotes(returnNotes)
                    .returnCondition(returnCondition)
                    .build();

            CommandResult<BookingResponseDto> result = commandExecutor.execute(command, borrowerId);

            if (!result.isSuccess()) {
                return error(HttpStatus.BAD_REQUEST, "Cannot complete booking",
                        result.getErrorMessage(), result.getErrorCode());
            }

            return ResponseEntity.ok(result.getData());
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (Exception e) {
            log.error("Error completing booking", e);
            return error(HttpStatus.BAD_REQUEST, "Cannot complete booking",
                    e.getMessage(), "BOOKING_COMPLETE_ERROR");
        }
    }

    /**
     * Cancel booking (can be done by borrower or owner)
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id,
                                     @RequestHeader("X-User-Id") String userId,
                                     @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        try {
            CancelBookingCommand command = CancelBookingCommand.builder()
                    .bookingId(id)
                    .userId(userId)
                    .cancellationReason(reason)
                    .build();

            CommandResult<BookingResponseDto> result = commandExecutor.execute(command, userId);

            if (!result.isSuccess()) {
                return error(HttpStatus.BAD_REQUEST, "Cannot cancel booking",
                        result.getErrorMessage(), result.getErrorCode());
            }

            return ResponseEntity.ok(result.getData());
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (Exception e) {
            log.error("Error cancelling booking", e);
            return error(HttpStatus.BAD_REQUEST, "Cannot cancel booking",
                    e.getMessage(), "BOOKING_CANCEL_ERROR");
        }
    }

    /**
     * Reject booking (owner rejects the booking request)
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id,
                                     @RequestHeader("X-User-Id") String ownerId,
                                     @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        try {
            RejectBookingCommand command = RejectBookingCommand.builder()
                    .bookingId(id)
                    .ownerId(ownerId)
                    .rejectionReason(reason)
                    .build();

            CommandResult<BookingResponseDto> result = commandExecutor.execute(command, ownerId);

            if (!result.isSuccess()) {
                return error(HttpStatus.BAD_REQUEST, "Cannot reject booking",
                        result.getErrorMessage(), result.getErrorCode());
            }

            return ResponseEntity.ok(result.getData());
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (Exception e) {
            log.error("Error rejecting booking", e);
            return error(HttpStatus.BAD_REQUEST, "Cannot reject booking",
                    e.getMessage(), "BOOKING_REJECT_ERROR");
        }
    }

    /**
     * Get all active bookings
     */
    @GetMapping("/active")
    public ResponseEntity<?> activeBookings() {
        return ResponseEntity.ok(queryService.getActiveBookings());
    }

    /**
     * Get all overdue bookings
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> overdueBookings() {
        return ResponseEntity.ok(queryService.getOverdueBookings());
    }

    /**
     * Get bookings by status for a user
     */
    @GetMapping("/by-status")
    public ResponseEntity<?> bookingsByStatus(@RequestParam String status,
                                              @RequestHeader("X-User-Id") String userId) {
        try {
            com.locallend.locallend.model.enums.BookingStatus bookingStatus =
                com.locallend.locallend.model.enums.BookingStatus.fromString(status);
            List<BookingResponseDto> bookings = queryService.getBookingsByStatus(bookingStatus, userId);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "Invalid status", e.getMessage(), "INVALID_STATUS");
        }
    }

    /**
     * Get active bookings for a specific item
     */
    @GetMapping("/item/{itemId}/active")
    public ResponseEntity<?> activeBookingsForItem(@PathVariable String itemId) {
        return ResponseEntity.ok(queryService.getActiveBookingsForItem(itemId));
    }

    /**
     * Error response helper
     */
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message,
                                                       String details, String code) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", message);
        resp.put("details", details);
        resp.put("error_code", code);
        return ResponseEntity.status(status).body(resp);
    }
}
