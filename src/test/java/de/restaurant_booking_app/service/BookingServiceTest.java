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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BookingService.class, TableService.class})
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TableService tableService;

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

        // Время для бронирования
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    void createBookingWithoutConflict() {
        // Создаем DTO для бронирования
        BookingDto bookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);

        // Создаем бронь
        Booking booking = bookingService.createBooking(bookingDto);

        // Проверяем результат
        assertNotNull(booking);
        assertNotNull(booking.getId());
        assertEquals(testTable.getId(), booking.getTable().getId());
        assertEquals(bookingDto.getCustomerName(), booking.getCustomerName());
        assertEquals(bookingDto.getCustomerEmail(), booking.getCustomerEmail());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        // Проверяем, что бронь сохранилась в базе
        List<Booking> bookings = bookingRepository.findByTableId(testTable.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void createBookingWithConflict() {
        // Создаем первую бронь
        BookingDto firstBookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);
        bookingService.createBooking(firstBookingDto);

        // Пытаемся создать вторую бронь на то же время
        BookingDto conflictingBookingDto = createTestBookingDto(
                testTable.getId(),
                startTime.plusMinutes(30), // перекрывается с первой
                endTime.plusMinutes(30)
        );

        // Проверяем, что выбрасывается ожидаемое исключение
        BookingConflictException exception = assertThrows(
                BookingConflictException.class,
                () -> bookingService.createBooking(conflictingBookingDto)
        );

        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("уже забронирован"));

        // Проверяем, что в базе осталась только одна бронь
        List<Booking> bookings = bookingRepository.findByTableId(testTable.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void cancelBooking() {
        // Создаем бронь
        BookingDto bookingDto = createTestBookingDto(testTable.getId(), startTime, endTime);
        Booking booking = bookingService.createBooking(bookingDto);

        // Отменяем бронь
        Booking cancelledBooking = bookingService.cancelBooking(booking.getId());

        // Проверяем статус
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Проверяем состояние в базе
        Booking fromDb = bookingRepository.findById(booking.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(BookingStatus.CANCELLED, fromDb.getStatus());
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
