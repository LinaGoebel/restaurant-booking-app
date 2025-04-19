package de.restaurant_booking_app.service;

import de.restaurant_booking_app.dto.BookingDto;
import de.restaurant_booking_app.exception.BookingConflictException;
import de.restaurant_booking_app.exception.ResourceNotFoundException;
import de.restaurant_booking_app.model.Booking;
import de.restaurant_booking_app.model.BookingStatus;
import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.repository.BookingRepository;
import de.restaurant_booking_app.repository.BookingTableRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    private final BookingTableRepository bookingTableRepository;
    private final BookingRepository bookingRepository;

    public BookingService(BookingTableRepository bookingTableRepository, BookingRepository bookingRepository) {
        this.bookingTableRepository = bookingTableRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking: " + id + " not found"));
    }

    public List<Booking> getBookingsByEmail(String email) {
        return bookingRepository.findByCustomerEmail(email);
    }

    public List<Booking> getTodayBookings() {
        return bookingRepository.findTodayBookings();
    }

    @Transactional
    public Booking createBooking(BookingDto bookingDto) {
        // Находим столик
        BookingTable table = bookingTableRepository.findById(bookingDto.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Столик с ID " + bookingDto.getTableId() + " не найден"));

        // Проверяем на конфликты
        if (hasConflict(table.getId(), bookingDto.getStartTime(), bookingDto.getEndTime(), null)) {
            throw new BookingConflictException("Столик #" + table.getTableNumber() +
                    " уже забронирован на указанное время");
        }
        // Создаем бронирование
        Booking booking = Booking.builder()
                .table(table)
                .startTime(bookingDto.getStartTime())
                .endTime(bookingDto.getEndTime())
                .customerName(bookingDto.getCustomerName())
                .customerEmail(bookingDto.getCustomerEmail())
                .customerPhone(bookingDto.getCustomerPhone())
                .status(BookingStatus.CONFIRMED)
                .build();

        return bookingRepository.save(booking);
    }

    /**
     * Отмена бронирования
     */
    @Transactional
    public Booking cancelBooking(Long id) {
        Booking booking = getBookingById(id);

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Обновление бронирования
     */
    @Transactional
    public Booking updateBooking(Long id, BookingDto bookingDto) {
        Booking existingBooking = getBookingById(id);

        // Находим столик
        BookingTable table = bookingTableRepository.findById(bookingDto.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Столик с ID " + bookingDto.getTableId() + " не найден"));

        // Проверяем на конфликты только если меняется столик или время
        if (!existingBooking.getTable().getId().equals(table.getId()) ||
                !existingBooking.getStartTime().equals(bookingDto.getStartTime()) ||
                !existingBooking.getEndTime().equals(bookingDto.getEndTime())) {

            if (hasConflict(table.getId(), bookingDto.getStartTime(), bookingDto.getEndTime(), id)) {
                throw new BookingConflictException("Столик #" + table.getTableNumber() +
                        " уже забронирован на указанное время");
            }
        }

        // Обновляем данные
        existingBooking.setTable(table);
        existingBooking.setStartTime(bookingDto.getStartTime());
        existingBooking.setEndTime(bookingDto.getEndTime());
        existingBooking.setCustomerName(bookingDto.getCustomerName());
        existingBooking.setCustomerEmail(bookingDto.getCustomerEmail());
        existingBooking.setCustomerPhone(bookingDto.getCustomerPhone());

        return bookingRepository.save(existingBooking);
    }

    /**
     * Проверка наличия конфликтов при бронировании
     */
    public boolean hasConflict(Long tableId, LocalDateTime startTime, LocalDateTime endTime, Long excludeBookingId) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                tableId, startTime, endTime);

        if (excludeBookingId != null) {
            return conflictingBookings.stream()
                    .anyMatch(booking -> !booking.getId().equals(excludeBookingId));
        }

        return !conflictingBookings.isEmpty();
    }

    /**
     * Поиск свободных столиков
     */
    public List<BookingTable> findAvailableTables(Integer capacity, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingTableRepository.findAvailableTables(capacity, startTime, endTime);
    }

    /**
     * Удаление бронирования (только для админа)
     */
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
    }

    /**
     * Планировщик для удаления старых бронирований (запускается каждый день в 3:00)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldBookings() {
        log.info("Запуск задачи по очистке старых бронирований");

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Booking> oldBookings = bookingRepository.findBookingsOlderThan(oneWeekAgo);

        if (!oldBookings.isEmpty()) {
            log.info("Найдено {} старых бронирований для удаления", oldBookings.size());
            bookingRepository.deleteAll(oldBookings);
            log.info("Старые бронирования успешно удалены");
        } else {
            log.info("Старых бронирований для удаления не найдено");
        }
    }


}
