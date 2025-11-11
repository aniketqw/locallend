package com.locallend.locallend.service;

import com.locallend.locallend.dto.request.BookingRequestDto;
import com.locallend.locallend.dto.response.BookingResponseDto;
import com.locallend.locallend.exception.BookingConflictException;
import com.locallend.locallend.exception.BookingNotFoundException;
import com.locallend.locallend.exception.InvalidBookingPeriodException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.repository.UserRepository;
import com.locallend.locallend.util.BookingFactory;
import com.locallend.locallend.util.BookingMapper;
import com.locallend.locallend.util.BookingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingFactory bookingFactory;
    private final BookingMapper bookingMapper;
    private final BookingValidator bookingValidator;

    public BookingService(BookingRepository bookingRepository,
                          ItemRepository itemRepository,
                          UserRepository userRepository,
                          BookingFactory bookingFactory,
                          BookingMapper bookingMapper,
                          BookingValidator bookingValidator) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingFactory = bookingFactory;
        this.bookingMapper = bookingMapper;
        this.bookingValidator = bookingValidator;
    }

    public BookingResponseDto createBooking(BookingRequestDto request, String borrowerId) {
        logger.info("Creating booking for item {} by borrower {}", request.getItemId(), borrowerId);

        // Fetch entities
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found"));
        User owner = item.getOwner();
        if (owner == null) {
            throw new IllegalArgumentException("Item has no owner");
        }

        // Comprehensive validation
        try {
            bookingValidator.validateAvailability(item, request.getStartDate(), request.getEndDate());
            bookingValidator.validateBorrowerEligibility(borrower, owner);
            bookingValidator.validateNoConflicts(item.getId(), request.getStartDate(), 
                                                 request.getEndDate(), bookingRepository);
        } catch (BookingConflictException | InvalidBookingPeriodException e) {
            logger.warn("Booking validation failed: {}", e.getMessage());
            throw e;
        }

        // Create booking
        Booking booking = bookingFactory.createFromRequest(
                item, borrower, owner,
                request.getStartDate(), request.getEndDate(),
                request.getBookingNotes(), request.getDepositAmount()
        );

        Booking saved = bookingRepository.save(booking);
        logger.info("Booking {} created successfully", saved.getId());
        return bookingMapper.toBookingResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(String id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        return bookingMapper.toBookingResponseDto(b);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsForBorrower(String borrowerId) {
        return bookingMapper.toBookingResponseDtoList(bookingRepository.findByBorrowerId(borrowerId));
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsForOwner(String ownerId) {
        return bookingMapper.toBookingResponseDtoList(bookingRepository.findByOwnerId(ownerId));
    }

    public BookingResponseDto confirm(String bookingId, String ownerId, String ownerNotes) {
        logger.info("Owner {} confirming booking {}", ownerId, bookingId);
        
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        bookingValidator.validateAuthorization(b, ownerId, "OWNER");
        bookingValidator.validateStatusTransition(b, com.locallend.locallend.model.enums.BookingStatus.CONFIRMED);
        
        b.confirm(ownerNotes);
        Booking saved = bookingRepository.save(b);
        logger.info("Booking {} confirmed", bookingId);
        return bookingMapper.toBookingResponseDto(saved);
    }

    public BookingResponseDto activate(String bookingId, String borrowerId) {
        logger.info("Borrower {} activating booking {}", borrowerId, bookingId);
        
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        bookingValidator.validateAuthorization(b, borrowerId, "BORROWER");
        bookingValidator.validateStatusTransition(b, com.locallend.locallend.model.enums.BookingStatus.ACTIVE);
        
        b.activate();
        
        // Mark item as borrowed
        Item item = b.getItem();
        if (item != null) {
            try {
                item.markAsBorrowed();
                itemRepository.save(item);
            } catch (IllegalStateException e) {
                logger.warn("Could not mark item as borrowed: {}", e.getMessage());
            }
        }
        
        Booking saved = bookingRepository.save(b);
        logger.info("Booking {} activated", bookingId);
        return bookingMapper.toBookingResponseDto(saved);
    }

    public BookingResponseDto complete(String bookingId, String borrowerId) {
        logger.info("Borrower {} completing booking {}", borrowerId, bookingId);
        
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        bookingValidator.validateAuthorization(b, borrowerId, "BORROWER");
        bookingValidator.validateStatusTransition(b, com.locallend.locallend.model.enums.BookingStatus.COMPLETED);
        
        b.complete();
        
        // Mark item as available again
        Item item = b.getItem();
        if (item != null && item.getStatus() == com.locallend.locallend.model.enums.ItemStatus.BORROWED) {
            item.setStatus(com.locallend.locallend.model.enums.ItemStatus.AVAILABLE);
            itemRepository.save(item);
        }
        
        Booking saved = bookingRepository.save(b);
        logger.info("Booking {} completed", bookingId);
        return bookingMapper.toBookingResponseDto(saved);
    }

    public BookingResponseDto cancel(String bookingId, String borrowerId, String reason) {
        logger.info("Borrower {} cancelling booking {}", borrowerId, bookingId);
        
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        bookingValidator.validateAuthorization(b, borrowerId, "BORROWER");
        
        if (!b.getStatus().canBeCancelled()) {
            throw new IllegalStateException("Booking cannot be cancelled in current status: " + b.getStatus());
        }
        
        b.cancel(reason != null ? reason : "Cancelled by borrower");
        Booking saved = bookingRepository.save(b);
        logger.info("Booking {} cancelled", bookingId);
        return bookingMapper.toBookingResponseDto(saved);
    }

    public BookingResponseDto reject(String bookingId, String ownerId, String reason) {
        logger.info("Owner {} rejecting booking {}", ownerId, bookingId);
        
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        bookingValidator.validateAuthorization(b, ownerId, "OWNER");
        
        b.reject(reason != null ? reason : "Rejected by owner");
        Booking saved = bookingRepository.save(b);
        logger.info("Booking {} rejected", bookingId);
        return bookingMapper.toBookingResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getActiveBookings() {
        return bookingMapper.toBookingResponseDtoList(bookingRepository.findActiveBookings());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getOverdueBookings() {
        return bookingMapper.toBookingResponseDtoList(bookingRepository.findOverdueBookings(LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByStatus(com.locallend.locallend.model.enums.BookingStatus status, String userId) {
        logger.info("Fetching bookings with status {} for user {}", status, userId);
        List<Booking> bookings = bookingRepository.findByBorrowerIdAndStatusOrderByCreatedDateDesc(userId, status);
        return bookingMapper.toBookingResponseDtoList(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getActiveBookingsForItem(String itemId) {
        logger.info("Fetching active bookings for item {}", itemId);
        List<Booking> bookings = bookingRepository.findActiveBookings();
        return bookingMapper.toBookingResponseDtoList(
            bookings.stream().filter(b -> itemId.equals(b.getItemId())).toList()
        );
    }

    @Transactional(readOnly = true)
    public long countCompletedBookings(String userId, boolean asOwner, LocalDateTime start, LocalDateTime end) {
        if (asOwner) {
            return bookingRepository.countCompletedBookingsByOwnerInPeriod(userId, start, end);
        } else {
            return bookingRepository.countCompletedBookingsByBorrowerInPeriod(userId, start, end);
        }
    }

    /**
     * Update booking status with proper validation.
     */
    public BookingResponseDto updateBookingStatus(String bookingId, 
                                                  com.locallend.locallend.model.enums.BookingStatus newStatus, 
                                                  String userId) {
        logger.info("Updating booking {} to status {}", bookingId, newStatus);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        bookingValidator.validateAuthorization(booking, userId, "EITHER");
        bookingValidator.validateStatusTransition(booking, newStatus);
        
        booking.setStatus(newStatus);
        booking.setUpdatedDate(LocalDateTime.now());
        
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(saved);
    }

    /**
     * Process overdue bookings - mark ACTIVE bookings past end date as OVERDUE.
     */
    @Transactional
    public int processOverdueBookings() {
        logger.info("Processing overdue bookings");
        List<Booking> overdueBookings = bookingRepository.findOverdueBookings(LocalDateTime.now());
        
        int count = 0;
        for (Booking booking : overdueBookings) {
            if (booking.getStatus() == com.locallend.locallend.model.enums.BookingStatus.ACTIVE) {
                booking.markOverdue();
                bookingRepository.save(booking);
                count++;
            }
        }
        
        logger.info("Marked {} bookings as overdue", count);
        return count;
    }
}
