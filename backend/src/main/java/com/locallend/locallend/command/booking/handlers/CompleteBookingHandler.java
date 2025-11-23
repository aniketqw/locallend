package com.locallend.locallend.command.booking.handlers;

import com.locallend.locallend.command.booking.CompleteBookingCommand;
import com.locallend.locallend.command.core.CommandContext;
import com.locallend.locallend.command.core.CommandHandler;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.exception.UnauthorizedAccessException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.enums.ItemStatus;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.state.booking.BookingStateMachine;
import com.locallend.locallend.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for completing an active booking.
 * This happens when the borrower returns the item.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CompleteBookingHandler implements CommandHandler<CompleteBookingCommand, BookingResponseDto> {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponseDto handle(CompleteBookingCommand command, CommandContext context) {
        log.info("Completing booking: {} by borrower: {}", command.getBookingId(), command.getBorrowerId());

        // 1. Fetch the booking
        Booking booking = bookingRepository.findById(command.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + command.getBookingId()));

        // 2. Verify borrower authorization
        if (!booking.getBorrowerId().equals(command.getBorrowerId())) {
            throw new UnauthorizedAccessException("Only the borrower can complete this booking");
        }

        // 3. Update item status back to available
        final String itemId = booking.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BookingNotFoundException("Item not found: " + itemId));

        item.setStatus(ItemStatus.AVAILABLE);
        itemRepository.save(item);

        // 4. Use state machine to handle completion
        BookingStateMachine stateMachine = new BookingStateMachine(booking, eventPublisher);
        stateMachine.complete(command.getBorrowerId(), command.getReturnCondition());

        // 5. Update return details
        booking.setActualEndDate(command.getActualEndDate());
        if (command.getReturnNotes() != null) {
            booking.setBookingNotes(booking.getBookingNotes() != null ?
                    booking.getBookingNotes() + "\nReturn notes: " + command.getReturnNotes() :
                    "Return notes: " + command.getReturnNotes());
        }

        // 6. Handle deposit refund
        if (command.isRefundDeposit()) {
            log.info("Deposit refund approved for booking {}", booking.getId());
            // In a real system, this would trigger the actual refund process
        }

        // 7. Save the updated booking
        booking = bookingRepository.save(booking);
        log.info("Booking {} completed successfully", booking.getId());

        // 8. Enable rating for this booking
        booking.setIsRated(false); // Can now be rated

        // 9. Map to response DTO
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public Class<CompleteBookingCommand> getCommandType() {
        return CompleteBookingCommand.class;
    }
}