package com.locallend.locallend.controller;

import com.locallend.locallend.dto.request.BookingRequestDto;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.exception.BookingConflictException;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.exception.InvalidBookingPeriodException;
import com.locallend.locallend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequestDto request,
                                           @RequestHeader("X-User-Id") String borrowerId) {
        try {
            BookingResponseDto dto = bookingService.createBooking(request, borrowerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (BookingConflictException e) {
            return error(HttpStatus.CONFLICT, "Booking conflict", e.getMessage(), "BOOKING_CONFLICT");
        } catch (InvalidBookingPeriodException e) {
            return error(HttpStatus.BAD_REQUEST, "Invalid booking period", e.getMessage(), "INVALID_PERIOD");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Booking creation failed", e.getMessage(), "BOOKING_CREATE_ERROR");
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", e.getMessage(), "INTERNAL_ERROR");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable String id) {
        try {
            BookingResponseDto dto = bookingService.getBookingById(id);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        }
    }

    /**
     * Get borrower's bookings (Issue #15 naming)
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<?> myBookings(@RequestHeader("X-User-Id") String userId) {
        List<BookingResponseDto> list = bookingService.getBookingsForBorrower(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get borrower's bookings (original endpoint)
     */
    @GetMapping("/my")
    public ResponseEntity<?> myBookingsOriginal(@RequestHeader("X-User-Id") String userId) {
        List<BookingResponseDto> list = bookingService.getBookingsForBorrower(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get pending approvals for owner (Issue #15 naming)
     */
    @GetMapping("/pending-approvals")
    public ResponseEntity<?> pendingApprovals(@RequestHeader("X-User-Id") String ownerId) {
        try {
            com.locallend.locallend.model.enums.BookingStatus pendingStatus = 
                com.locallend.locallend.model.enums.BookingStatus.PENDING;
            List<BookingResponseDto> bookings = bookingService.getBookingsForOwner(ownerId);
            // Filter for PENDING status
            List<BookingResponseDto> pending = bookings.stream()
                .filter(b -> b.getStatus() == pendingStatus)
                .toList();
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching pending approvals", e.getMessage(), "FETCH_ERROR");
        }
    }

    /**
     * Get owner's bookings (original endpoint)
     */
    @GetMapping("/my-owned")
    public ResponseEntity<?> myOwnedBookings(@RequestHeader("X-User-Id") String ownerId) {
        List<BookingResponseDto> list = bookingService.getBookingsForOwner(ownerId);
        return ResponseEntity.ok(list);
    }

    /**
     * Approve booking - Issue #15 naming (alias for confirm)
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id,
                                     @RequestHeader("X-User-Id") String ownerId,
                                     @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("ownerNotes") : null;
        try {
            BookingResponseDto dto = bookingService.confirm(id, ownerId, notes);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Cannot approve booking", e.getMessage(), "BOOKING_APPROVE_ERROR");
        }
    }

    /**
     * Confirm booking - original endpoint
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable String id,
                                      @RequestHeader("X-User-Id") String ownerId,
                                      @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("ownerNotes") : null;
        try {
            BookingResponseDto dto = bookingService.confirm(id, ownerId, notes);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Cannot confirm booking", e.getMessage(), "BOOKING_CONFIRM_ERROR");
        }
    }

    /**
     * Start booking - Issue #15 naming (alias for activate)
     */
    @PatchMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable String id,
                                    @RequestHeader("X-User-Id") String borrowerId) {
        try {
            BookingResponseDto dto = bookingService.activate(id, borrowerId);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Cannot start booking", e.getMessage(), "BOOKING_START_ERROR");
        }
    }

    /**
     * Activate booking - original endpoint
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable String id,
                                       @RequestHeader("X-User-Id") String borrowerId) {
        try {
            BookingResponseDto dto = bookingService.activate(id, borrowerId);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Cannot activate booking", e.getMessage(), "BOOKING_ACTIVATE_ERROR");
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable String id,
                                       @RequestHeader("X-User-Id") String borrowerId) {
        try {
            BookingResponseDto dto = bookingService.complete(id, borrowerId);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Cannot complete booking", e.getMessage(), "BOOKING_COMPLETE_ERROR");
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id,
                                     @RequestHeader("X-User-Id") String borrowerId,
                                     @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        try {
            BookingResponseDto dto = bookingService.cancel(id, borrowerId, reason);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Cannot cancel booking", e.getMessage(), "BOOKING_CANCEL_ERROR");
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id,
                                     @RequestHeader("X-User-Id") String ownerId,
                                     @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        try {
            BookingResponseDto dto = bookingService.reject(id, ownerId, reason);
            return ResponseEntity.ok(dto);
        } catch (BookingNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, "Booking not found", e.getMessage(), "BOOKING_NOT_FOUND");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, "Cannot reject booking", e.getMessage(), "BOOKING_REJECT_ERROR");
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> activeBookings() {
        return ResponseEntity.ok(bookingService.getActiveBookings());
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> overdueBookings() {
        return ResponseEntity.ok(bookingService.getOverdueBookings());
    }

    /**
     * Get bookings for a specific user as borrower (frontend compatibility endpoint)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsForUser(@PathVariable String userId) {
        try {
            List<BookingResponseDto> bookings = bookingService.getBookingsForBorrower(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching user bookings", e.getMessage(), "USER_BOOKINGS_ERROR");
        }
    }

    /**
     * Get bookings for a specific user as owner (frontend compatibility endpoint)
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getBookingsForOwner(@PathVariable String ownerId) {
        try {
            List<BookingResponseDto> bookings = bookingService.getBookingsForOwner(ownerId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching owner bookings", e.getMessage(), "OWNER_BOOKINGS_ERROR");
        }
    }

    @GetMapping("/by-status")
    public ResponseEntity<?> bookingsByStatus(@RequestParam String status,
                                              @RequestHeader("X-User-Id") String userId) {
        try {
            com.locallend.locallend.model.enums.BookingStatus bookingStatus = 
                com.locallend.locallend.model.enums.BookingStatus.fromString(status);
            List<BookingResponseDto> bookings = bookingService.getBookingsByStatus(bookingStatus, userId);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "Invalid status", e.getMessage(), "INVALID_STATUS");
        }
    }

    @GetMapping("/item/{itemId}/active")
    public ResponseEntity<?> activeBookingsForItem(@PathVariable String itemId) {
        return ResponseEntity.ok(bookingService.getActiveBookingsForItem(itemId));
    }

    @PostMapping("/process-overdue")
    public ResponseEntity<?> processOverdue() {
        int count = bookingService.processOverdueBookings();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Processed overdue bookings");
        resp.put("count", count);
        return ResponseEntity.ok(resp);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message, String details, String code) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", message);
        resp.put("details", details);
        resp.put("error_code", code);
        return ResponseEntity.status(status).body(resp);
    }
}
