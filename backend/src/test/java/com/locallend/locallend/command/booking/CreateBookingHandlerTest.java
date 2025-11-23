package com.locallend.locallend.command.booking;

import com.locallend.locallend.command.booking.handlers.CreateBookingHandler;
import com.locallend.locallend.command.core.CommandContext;
import com.locallend.locallend.dto.BookingResponseDto;
import com.locallend.locallend.event.EventPublisher;
import com.locallend.locallend.event.booking.BookingCreatedEvent;
import com.locallend.locallend.exception.BookingConflictException;
import com.locallend.locallend.exception.InsufficientTrustScoreException;
import com.locallend.locallend.exception.ItemNotAvailableException;
import com.locallend.locallend.model.Booking;
import com.locallend.locallend.model.Item;
import com.locallend.locallend.model.User;
import com.locallend.locallend.model.enums.BookingStatus;
import com.locallend.locallend.repository.BookingRepository;
import com.locallend.locallend.repository.ItemRepository;
import com.locallend.locallend.repository.UserRepository;
import com.locallend.locallend.util.BookingFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateBookingHandler demonstrating Command Pattern testing.
 * Shows how to test the new architecture with proper isolation.
 */
@ExtendWith(MockitoExtension.class)
class CreateBookingHandlerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingFactory bookingFactory;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CreateBookingHandler handler;

    private CreateBookingCommand command;
    private CommandContext context;
    private Item mockItem;
    private User mockBorrower;
    private User mockOwner;
    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);

        command = CreateBookingCommand.builder()
                .itemId("item123")
                .borrowerId("borrower123")
                .startDate(startDate)
                .endDate(endDate)
                .bookingNotes("Test booking")
                .depositAmount(new BigDecimal("50.00"))
                .build();

        context = CommandContext.builder()
                .userId("borrower123")
                .username("testborrower")
                .correlationId("corr-123")
                .build();

        // Setup mock entities
        mockOwner = new User();
        mockOwner.setId("owner123");
        mockOwner.setUsername("owner");
        mockOwner.setTrustScore(4.5);
        mockOwner.setActive(true);

        mockItem = new Item();
        mockItem.setId("item123");
        mockItem.setName("Test Item");
        mockItem.setOwner(mockOwner);
        mockItem.setActive(true);
        mockItem.setStatus(com.locallend.locallend.model.ItemStatus.AVAILABLE);

        mockBorrower = new User();
        mockBorrower.setId("borrower123");
        mockBorrower.setUsername("borrower");
        mockBorrower.setTrustScore(4.0);
        mockBorrower.setActive(true);

        mockBooking = Booking.builder()
                .id("booking123")
                .item(mockItem)
                .itemId(mockItem.getId())
                .borrower(mockBorrower)
                .borrowerId(mockBorrower.getId())
                .owner(mockOwner)
                .ownerId(mockOwner.getId())
                .status(BookingStatus.PENDING)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Test
    void testHandle_SuccessfulBookingCreation() {
        // Given
        when(itemRepository.findById("item123")).thenReturn(Optional.of(mockItem));
        when(userRepository.findById("borrower123")).thenReturn(Optional.of(mockBorrower));
        when(userRepository.findById("owner123")).thenReturn(Optional.of(mockOwner));
        when(bookingRepository.findConflictingBookings(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // When
        BookingResponseDto result = handler.handle(command, context);

        // Then
        assertNotNull(result);
        assertEquals("booking123", result.getId());
        assertEquals(BookingStatus.PENDING, result.getStatus());

        // Verify repository calls
        verify(itemRepository).findById("item123");
        verify(userRepository).findById("borrower123");
        verify(userRepository).findById("owner123");
        verify(bookingRepository).findConflictingBookings(
                eq("item123"), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingRepository).save(any(Booking.class));

        // Verify event publication
        ArgumentCaptor<BookingCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(BookingCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        BookingCreatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals("booking123", publishedEvent.getBookingId());
        assertEquals("item123", publishedEvent.getItemId());
        assertEquals("borrower123", publishedEvent.getBorrowerId());
        assertEquals("owner123", publishedEvent.getOwnerId());
    }

    @Test
    void testHandle_ItemNotAvailable_ThrowsException() {
        // Given
        mockItem.setStatus(com.locallend.locallend.model.ItemStatus.BORROWED);
        when(itemRepository.findById("item123")).thenReturn(Optional.of(mockItem));

        // When & Then
        assertThrows(ItemNotAvailableException.class, () -> {
            handler.handle(command, context);
        });

        // Verify no booking was saved
        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void testHandle_InsufficientTrustScore_ThrowsException() {
        // Given
        mockBorrower.setTrustScore(2.5); // Below minimum
        when(itemRepository.findById("item123")).thenReturn(Optional.of(mockItem));
        when(userRepository.findById("borrower123")).thenReturn(Optional.of(mockBorrower));

        // When & Then
        assertThrows(InsufficientTrustScoreException.class, () -> {
            handler.handle(command, context);
        });

        // Verify no booking was saved
        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void testHandle_BookingConflict_ThrowsException() {
        // Given
        when(itemRepository.findById("item123")).thenReturn(Optional.of(mockItem));
        when(userRepository.findById("borrower123")).thenReturn(Optional.of(mockBorrower));
        when(userRepository.findById("owner123")).thenReturn(Optional.of(mockOwner));

        // Simulate existing conflicting booking
        Booking conflictingBooking = Booking.builder()
                .id("conflict123")
                .itemId("item123")
                .status(BookingStatus.CONFIRMED)
                .build();
        when(bookingRepository.findConflictingBookings(anyString(), any(), any()))
                .thenReturn(Collections.singletonList(conflictingBooking));

        // When & Then
        assertThrows(BookingConflictException.class, () -> {
            handler.handle(command, context);
        });

        // Verify no booking was saved
        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void testHandle_BorrowerCannotBorrowOwnItem() {
        // Given - borrower is the owner
        mockItem.setOwner(mockBorrower);
        when(itemRepository.findById("item123")).thenReturn(Optional.of(mockItem));
        when(userRepository.findById("borrower123")).thenReturn(Optional.of(mockBorrower));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            handler.handle(command, context);
        });
        assertTrue(exception.getMessage().contains("Cannot borrow your own items"));

        // Verify no booking was saved
        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void testHandle_InvalidDateRange_ThrowsException() {
        // Given - end date before start date
        command = CreateBookingCommand.builder()
                .itemId("item123")
                .borrowerId("borrower123")
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(1)) // Invalid: end before start
                .build();

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            handler.handle(command, context);
        });
        assertTrue(exception.getMessage().contains("End date must be after start date"));

        // Verify no repositories were called
        verify(itemRepository, never()).findById(anyString());
        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}