package de.restaurant_booking_app.service;

import de.restaurant_booking_app.dto.BookingDto;
import de.restaurant_booking_app.exception.BookingConflictException;
import de.restaurant_booking_app.model.Booking;
import de.restaurant_booking_app.model.BookingStatus;
import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.repository.BookingRepository;
import de.restaurant_booking_app.repository.BookingTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingTableRepository tableRepository;

    @Mock
    private EmailService emailService;

    private BookingService bookingService;
    private BookingTable testTable;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        // Инициализация сервиса с моками
        bookingService = new BookingService(tableRepository, bookingRepository, emailService);

        // Настройка тестовых данных
        testTable = new BookingTable();
        testTable.setId(1L);
        testTable.setTableNumber(99);
        testTable.setCapacity(4);
        testTable.setIsVip(false);

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);

        // Не делаем лишних заглушек здесь, будем настраивать их в каждом тесте отдельно
    }

    @Test
    void createBookingWithoutConflict() {
        // Настройка мока для поиска столика по ID
        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        // Настройка мока для проверки отсутствия конфликтов
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        // Настройка сохранения бронирования
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(100L);
            return booking;
        });
        // Не нужно беспокоиться об отправке писем в тесте
        doNothing().when(emailService).sendBookingConfirmation(any(Booking.class));

        // Создаем DTO для бронирования
        BookingDto bookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);

        // Вызываем тестируемый метод
        Booking booking = bookingService.createBooking(bookingDto);

        // Проверяем результат
        assertNotNull(booking);
        assertEquals(100L, booking.getId());
        assertEquals(testTable, booking.getTable());
        assertEquals(bookingDto.getCustomerName(), booking.getCustomerName());
        assertEquals(bookingDto.getCustomerEmail(), booking.getCustomerEmail());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        // Проверяем, что были вызваны нужные методы репозиториев
        verify(tableRepository).findById(testTable.getId());
        verify(bookingRepository).findConflictingBookings(eq(testTable.getId()), any(), any());
        verify(bookingRepository).save(any(Booking.class));
        verify(emailService).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    void createBookingWithConflict() {
        // Настройка мока для поиска столика по ID
        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        // Настройка мока для имитации конфликта
        List<Booking> conflictingBookings = new ArrayList<>();
        conflictingBookings.add(mock(Booking.class));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any()))
                .thenReturn(conflictingBookings);

        // Создаем DTO для бронирования
        BookingDto bookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);

        // Проверяем, что выбрасывается исключение
        BookingConflictException exception = assertThrows(
                BookingConflictException.class,
                () -> bookingService.createBooking(bookingDto)
        );

        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("уже забронирован"));

        // Проверяем, что save не был вызван
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(emailService, never()).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    void cancelBooking() {
        // Подготовка тестовых данных
        Booking existingBooking = new Booking();
        existingBooking.setId(1L);
        existingBooking.setTable(testTable);
        existingBooking.setStatus(BookingStatus.CONFIRMED);
        existingBooking.setCustomerName("Тестовый Клиент");
        existingBooking.setCustomerEmail("test@example.com");
        existingBooking.setStartTime(startTime);
        existingBooking.setEndTime(endTime);

        // Настройка моков
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendBookingCancellation(any(Booking.class));

        // Вызываем тестируемый метод
        Booking cancelledBooking = bookingService.cancelBooking(1L);

        // Проверяем результат
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Проверяем, что методы репозитория были вызваны
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(cancelledBooking);
        verify(emailService).sendBookingCancellation(any(Booking.class));
    }

    // Вспомогательный метод для создания тестового DTO
    private BookingDto createTestBookingDto(Long tableId, LocalDateTime start, LocalDateTime end) {
        return BookingDto.builder()
                .tableId(tableId)
                .startTime(start)
                .endTime(end)
                .customerName("Тестовый Клиент")
                .customerEmail("test@example.com")
                .customerPhone("+7 999 123 45 67")
                .build();
    }
}