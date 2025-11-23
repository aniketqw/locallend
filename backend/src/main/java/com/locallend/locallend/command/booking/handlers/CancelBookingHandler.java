package com.locallend.locallend.command.booking.handlers;

import com.locallend.locallend.command.booking.CancelBookingCommand;
import com.locallend.locallend.command.core.CommandContext;
import com.locallend.locallend.command.core.CommandHandler;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.exception.UnauthorizedAccessException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.state.booking.BookingStateMachine;
import com.locallend.locallend.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for cancelling a booking.
 * Can be done by either borrower or owner depending on the state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CancelBookingHandler implements CommandHandler<CancelBookingCommand, BookingResponseDto> {

    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponseDto handle(CancelBookingCommand command, CommandContext context) {
        log.info("Cancelling booking: {} by user: {}", command.getBookingId(), command.getUserId());

        // 1. Fetch the booking
        Booking booking = bookingRepository.findById(command.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + command.getBookingId()));

        // 2. Verify user authorization (must be borrower or owner)
        if (!booking.getBorrowerId().equals(command.getUserId()) &&
            !booking.getOwnerId().equals(command.getUserId()) &&
            !command.isSystemCancellation()) {
            throw new UnauthorizedAccessException("Only the borrower or owner can cancel this booking");
        }

        // 3. Use state machine to handle cancellation
        BookingStateMachine stateMachine = new BookingStateMachine(booking, eventPublisher);
        String userId = command.isSystemCancellation() ? "SYSTEM" : command.getUserId();
        stateMachine.cancel(userId, command.getCancellationReason());

        // 4. Save the cancellation reason
        booking.setBookingNotes(booking.getBookingNotes() != null ?
                booking.getBookingNotes() + "\nCancellation reason: " + command.getCancellationReason() :
                "Cancellation reason: " + command.getCancellationReason());

        // 5. Save the updated booking
        booking = bookingRepository.save(booking);
        log.info("Booking {} cancelled successfully. Reason: {}", booking.getId(), command.getCancellationReason());

        // 6. Send notifications if required
        if (command.isNotifyOtherParty()) {
            String otherPartyId = booking.getBorrowerId().equals(command.getUserId()) ?
                    booking.getOwnerId() : booking.getBorrowerId();
            log.info("Notifying user {} about booking cancellation", otherPartyId);
            // Notification logic would go here
        }

        // 7. Map to response DTO
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public Class<CancelBookingCommand> getCommandType() {
        return CancelBookingCommand.class;
    }
}