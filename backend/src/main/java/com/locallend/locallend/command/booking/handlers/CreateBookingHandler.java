package com.locallend.locallend.command.booking.handlers;

import com.locallend.locallend.command.booking.CreateBookingCommand;
import com.locallend.locallend.command.core.CommandContext;
import com.locallend.locallend.command.core.CommandHandler;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.event.booking.BookingCreatedEvent;
import com.locallend.locallend.exception.*;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.repository.UserRepository;
import com.locallend.locallend.util.BookingFactory;
import com.locallend.locallend.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handler for creating new bookings.
 * Validates availability, conflicts, and user eligibility before creating the booking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateBookingHandler implements CommandHandler<CreateBookingCommand, BookingResponseDto> {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingFactory bookingFactory;
    private final EventPublisher eventPublisher;

    private static final double MINIMUM_TRUST_SCORE = 3.0;

    @Override
    @Transactional
    public BookingResponseDto handle(CreateBookingCommand command, CommandContext context) {
        log.info("Creating booking for item: {} by user: {}", command.getItemId(), command.getBorrowerId());

        // 1. Validate the command data
        validateCommand(command);

        // 2. Fetch and validate item
        Item item = fetchAndValidateItem(command.getItemId());

        // 3. Fetch and validate borrower
        User borrower = fetchAndValidateBorrower(command.getBorrowerId());

        // 4. Fetch owner
        User owner = userRepository.findById(item.getOwner().getId())
                .orElseThrow(() -> new UserNotFoundException("Owner not found: " + item.getOwner().getId()));

        // 5. Validate borrower eligibility
        validateBorrowerEligibility(borrower, owner, item);

        // 6. Check for booking conflicts
        checkForConflicts(command);

        // 7. Create the booking entity
        Booking booking = createBookingEntity(command, item, borrower, owner);

        // 8. Initialize state machine (will be implemented with State Pattern)
        booking.setStatus(BookingStatus.PENDING);

        // 9. Save the booking
        booking = bookingRepository.save(booking);
        log.info("Booking created with ID: {}", booking.getId());

        // 10. Publish domain event
        if (eventPublisher != null) {
            eventPublisher.publish(new BookingCreatedEvent(
                    booking.getId(),
                    booking.getItemId(),
                    booking.getBorrowerId(),
                    booking.getOwnerId(),
                    booking.getStartDate(),
                    booking.getEndDate()
            ));
        }

        // 11. Map to response DTO
        return BookingMapper.toResponseDto(booking);
    }

    /**
     * Validates the command data.
     */
    private void validateCommand(CreateBookingCommand command) {
        if (!command.isValidDateRange()) {
            throw new InvalidBookingPeriodException("End date must be after start date");
        }

        if (command.getStartDate().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingPeriodException("Start date must be in the future");
        }

        if (command.getDurationInDays() > 30) {
            throw new InvalidBookingPeriodException("Booking duration cannot exceed 30 days");
        }
    }

    /**
     * Fetches and validates the item.
     */
    private Item fetchAndValidateItem(String itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + itemId));

        if (!item.isActive()) {
            throw new ItemNotAvailableException("Item is not active: " + itemId);
        }

        if (!item.canBeBorrowed()) {
            throw new ItemNotAvailableException("Item is not available for borrowing: " + itemId);
        }

        return item;
    }

    /**
     * Fetches and validates the borrower.
     */
    private User fetchAndValidateBorrower(String borrowerId) {
        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new UserNotFoundException("Borrower not found: " + borrowerId));

        if (!borrower.isActive()) {
            throw new BusinessException("Borrower account is not active");
        }

        return borrower;
    }

    /**
     * Validates borrower eligibility.
     */
    private void validateBorrowerEligibility(User borrower, User owner, Item item) {
        // Can't borrow own items
        if (borrower.getId().equals(owner.getId())) {
            throw new BusinessException("Cannot borrow your own items");
        }

        // Check trust score
        if (borrower.getTrustScore() < MINIMUM_TRUST_SCORE) {
            throw new InsufficientTrustScoreException(
                    borrower.getId(),
                    borrower.getTrustScore(),
                    MINIMUM_TRUST_SCORE
            );
        }

        // Additional eligibility checks can be added here
        // e.g., maximum active bookings, payment method verification, etc.
    }

    /**
     * Checks for booking conflicts.
     */
    private void checkForConflicts(CreateBookingCommand command) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                command.getItemId(),
                command.getStartDate(),
                command.getEndDate()
        );

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(
                    "Item is already booked for the requested period. " +
                            "Conflicting bookings: " + conflicts.size()
            );
        }
    }

    /**
     * Creates the booking entity.
     */
    private Booking createBookingEntity(CreateBookingCommand command, Item item, User borrower, User owner) {
        return Booking.builder()
                .item(item)
                .itemId(item.getId())
                .borrower(borrower)
                .borrowerId(borrower.getId())
                .owner(owner)
                .ownerId(owner.getId())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .bookingNotes(command.getBookingNotes())
                .depositAmount(command.getDepositAmount() != null ?
                        command.getDepositAmount().doubleValue() : item.getDeposit())
                .depositPaid(false)
                .isRated(false)
                .durationDays((int) command.getDurationInDays())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    @Override
    public Class<CreateBookingCommand> getCommandType() {
        return CreateBookingCommand.class;
    }
}