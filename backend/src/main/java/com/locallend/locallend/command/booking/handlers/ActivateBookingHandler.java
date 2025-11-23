package com.locallend.locallend.command.booking.handlers;

import com.locallend.locallend.command.booking.ActivateBookingCommand;
import com.locallend.locallend.command.core.CommandContext;
import com.locallend.locallend.command.core.CommandHandler;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.exception.ItemNotAvailableException;
import com.locallend.locallend.exception.UnauthorizedAccessException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.enums.ItemStatus;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.state.booking.BookingStateMachine;
import com.locallend.locallend.state.booking.BookingStateContext;
import com.locallend.locallend.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for activating a confirmed booking.
 * This happens when the borrower picks up the item.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivateBookingHandler implements CommandHandler<ActivateBookingCommand, BookingResponseDto> {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponseDto handle(ActivateBookingCommand command, CommandContext context) {
        log.info("Activating booking: {} by borrower: {}", command.getBookingId(), command.getBorrowerId());

        // 1. Fetch the booking
        Booking booking = bookingRepository.findById(command.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + command.getBookingId()));

        // 2. Verify borrower authorization
        if (!booking.getBorrowerId().equals(command.getBorrowerId())) {
            throw new UnauthorizedAccessException("Only the borrower can activate this booking");
        }

        // 3. Verify and update item status
        final String itemId = booking.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotAvailableException("Item not found: " + itemId));

        if (item.getStatus() == ItemStatus.BORROWED) {
            throw new ItemNotAvailableException("Item is already borrowed");
        }

        // 4. Mark item as borrowed
        item.setStatus(ItemStatus.BORROWED);
        itemRepository.save(item);

        // 5. Use state machine to handle activation
        BookingStateMachine stateMachine = new BookingStateMachine(booking, eventPublisher);
        BookingStateContext stateContext = new BookingStateContext(booking, stateMachine, eventPublisher)
                .withUser(command.getBorrowerId())
                .withReason("Item picked up by borrower")
                .withMetadata("actualStartDate", command.getActualStartDate())
                .withMetadata("depositPaid", command.isDepositPaid())
                .withMetadata("pickupNotes", command.getPickupNotes());

        stateMachine.activate(command.getBorrowerId());

        // 6. Update deposit payment status
        if (command.isDepositPaid()) {
            booking.setDepositPaid(true);
        }

        // 7. Save the updated booking
        booking = bookingRepository.save(booking);
        log.info("Booking {} activated successfully", booking.getId());

        // 8. Map to response DTO
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public Class<ActivateBookingCommand> getCommandType() {
        return ActivateBookingCommand.class;
    }
}