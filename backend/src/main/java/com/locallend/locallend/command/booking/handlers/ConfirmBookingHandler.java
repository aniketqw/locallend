package com.locallend.locallend.command.booking.handlers;

import com.locallend.locallend.command.booking.ConfirmBookingCommand;
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
 * Handler for confirming/approving a pending booking.
 * Only the item owner can confirm a booking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmBookingHandler implements CommandHandler<ConfirmBookingCommand, BookingResponseDto> {

    private final BookingRepository bookingRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponseDto handle(ConfirmBookingCommand command, CommandContext context) {
        log.info("Confirming booking: {} by owner: {}", command.getBookingId(), command.getOwnerId());

        // 1. Fetch the booking
        Booking booking = bookingRepository.findById(command.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + command.getBookingId()));

        // 2. Verify owner authorization
        if (!booking.getOwnerId().equals(command.getOwnerId())) {
            throw new UnauthorizedAccessException("Only the owner can confirm this booking");
        }

        // 3. Create and use state machine to handle transition
        BookingStateMachine stateMachine = new BookingStateMachine(booking, eventPublisher);
        stateMachine.confirm(command.getOwnerId(), command.getOwnerNotes());

        // 4. Save the updated booking
        booking = bookingRepository.save(booking);
        log.info("Booking {} confirmed successfully", booking.getId());

        // 5. Map to response DTO
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public Class<ConfirmBookingCommand> getCommandType() {
        return ConfirmBookingCommand.class;
    }
}