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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // Используем специальный профиль для тестов
public class BookingServiceIntegrationTest {

    @TestConfiguration
    static class IntegrationTestConfig {
        // Мок EmailService для тестов
        @Bean
        @Primary
        public EmailService emailService() {
            EmailService mockEmailService = mock(EmailService.class);
            doNothing().when(mockEmailService).sendBookingConfirmation(any(Booking.class));
            doNothing().when(mockEmailService).sendBookingCancellation(any(Booking.class));
            doNothing().when(mockEmailService).sendBookingUpdate(any(Booking.class));
            return mockEmailService;
        }

        // Мок EmailReceiverService для тестов, чтобы избежать проблемы с 'mail.imap.host'
        @Bean
        @Primary
        public EmailReceiverService emailReceiverService() {
            return mock(EmailReceiverService.class);
        }
    }

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingTableRepository tableRepository;

    private BookingTable testTable;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        // Очищаем данные перед каждым тестом
        bookingRepository.deleteAll();
        tableRepository.deleteAll();

        // Создаем тестовый столик
        testTable = BookingTable.builder()
                .tableNumber(99)
                .capacity(4)
                .isVip(false)
                .build();
        testTable = tableRepository.save(testTable);

        // Устанавливаем время для бронирования
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void createBookingWithoutConflict() {
        // Создаем DTO для бронирования
        BookingDto bookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);

        // Выполняем бронирование
        Booking booking = bookingService.createBooking(bookingDto);

        // Проверяем, что бронирование создано успешно
        assertNotNull(booking);
        assertNotNull(booking.getId());
        assertEquals(testTable.getId(), booking.getTable().getId());
        assertEquals(bookingDto.getCustomerName(), booking.getCustomerName());
        assertEquals(bookingDto.getCustomerEmail(), booking.getCustomerEmail());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        // Проверяем, что бронирование сохранено в базе
        List<Booking> bookings = bookingRepository.findByTableId(testTable.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void createBookingWithConflict() {
        // Сначала создаем первое бронирование
        BookingDto firstBookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);
        bookingService.createBooking(firstBookingDto);

        // Пытаемся создать второе бронирование на то же время
        BookingDto conflictingBookingDto = createTestBookingDto(
                testTable.getId(),
                startTime.plusMinutes(30), // перекрывается с первым
                endTime.plusMinutes(30)
        );

        // Проверяем, что выбрасывается исключение
        BookingConflictException exception = assertThrows(
                BookingConflictException.class,
                () -> bookingService.createBooking(conflictingBookingDto)
        );

        // Проверяем сообщение исключения
        assertTrue(exception.getMessage().contains("уже забронирован"));

        // Проверяем, что в базе осталось только одно бронирование
        List<Booking> bookings = bookingRepository.findByTableId(testTable.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void cancelBooking() {
        // Создаем бронирование
        BookingDto bookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);
        Booking booking = bookingService.createBooking(bookingDto);

        // Отменяем бронирование
        Booking cancelledBooking = bookingService.cancelBooking(booking.getId());

        // Проверяем, что бронирование отменено
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Проверяем, что состояние сохранено в базе
        Booking fromDb = bookingRepository.findById(booking.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(BookingStatus.CANCELLED, fromDb.getStatus());
    }

    // Вспомогательный метод для создания тестового DTO бронирования
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