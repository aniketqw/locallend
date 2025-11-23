package com.locallend.locallend.command.booking.handlers;

import com.locallend.locallend.command.booking.RejectBookingCommand;
import com.locallend.locallend.command.core.CommandContext;
import com.locallend.locallend.command.core.CommandHandler;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.exception.UnauthorizedAccessException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.state.booking.BookingStateMachine;
import com.locallend.locallend.state.booking.BookingStateContext;
import com.locallend.locallend.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for rejecting a pending booking.
 * Only the owner can reject a booking request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RejectBookingHandler implements CommandHandler<RejectBookingCommand, BookingResponseDto> {

    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponseDto handle(RejectBookingCommand command, CommandContext context) {
        log.info("Rejecting booking: {} by owner: {}", command.getBookingId(), command.getOwnerId());

        // 1. Fetch the booking
        Booking booking = bookingRepository.findById(command.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + command.getBookingId()));

        // 2. Verify owner authorization
        if (!booking.getOwnerId().equals(command.getOwnerId())) {
            throw new UnauthorizedAccessException("Only the owner can reject this booking");
        }

        // 3. Create state context with metadata
        BookingStateMachine stateMachine = new BookingStateMachine(booking, eventPublisher);
        BookingStateContext stateContext = new BookingStateContext(booking, stateMachine, eventPublisher)
                .withUser(command.getOwnerId())
                .withReason(command.getRejectionReason())
                .withMetadata("suggestAlternatives", command.isSuggestAlternatives());

        // 4. Use state machine to handle rejection
        stateMachine.reject(command.getOwnerId(), command.getRejectionReason());

        // 5. Save the rejection reason
        booking.setOwnerNotes("Rejection reason: " + command.getRejectionReason());

        // 6. Save the updated booking
        booking = bookingRepository.save(booking);
        log.info("Booking {} rejected successfully. Reason: {}", booking.getId(), command.getRejectionReason());

        // 7. Handle notifications
        if (command.isNotifyBorrower()) {
            log.info("Notifying borrower {} about booking rejection", booking.getBorrowerId());
            // Notification logic would go here
        }

        // 8. Suggest alternatives if requested
        if (command.isSuggestAlternatives()) {
            log.info("Suggesting alternative dates for rejected booking {}", booking.getId());
            // Logic to find and suggest alternative available dates
            // This could query for available slots and send suggestions to the borrower
        }

        // 9. Map to response DTO
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public Class<RejectBookingCommand> getCommandType() {
        return RejectBookingCommand.class;
    }
}