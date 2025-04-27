package de.restaurant_booking_app.service;

import de.restaurant_booking_app.dto.BookingDto;
import de.restaurant_booking_app.exception.BookingConflictException;
import de.restaurant_booking_app.exception.InvalidBookingException;
import de.restaurant_booking_app.exception.ResourceNotFoundException;
import de.restaurant_booking_app.model.Booking;
import de.restaurant_booking_app.model.BookingStatus;
import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.repository.BookingRepository;
import de.restaurant_booking_app.repository.BookingTableRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    private final BookingTableRepository bookingTableRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final String adminPhone;
    private final ZoneId serverZoneId = ZoneId.systemDefault();

    public BookingService(BookingTableRepository bookingTableRepository,
                          BookingRepository bookingRepository,
                          EmailService emailService,
                          NotificationService notificationService,
                          Environment env) {
        this.bookingTableRepository = bookingTableRepository;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.adminPhone = env.getProperty("admin.phone");
        log.info("BookingService инициализирован. Часовой пояс сервера: {}", serverZoneId);
    }

    public List<Booking> getAllBookings() {
        log.debug("Получение всех бронирований");
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        log.debug("Получение бронирования по ID: {}", id);
        return bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Бронирование с ID {} не найдено", id);
                    return new ResourceNotFoundException("Бронирование с ID " + id + " не найдено");
                });
    }

    public List<Booking> getBookingsByEmail(String email) {
        log.debug("Получение бронирований по email: {}", email);
        return bookingRepository.findByCustomerEmail(email);
    }

    public List<Booking> getTodayBookings() {
        log.debug("Получение сегодняшних бронирований");
        return bookingRepository.findTodayBookings();
    }

    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Получение бронирований с {} по {}", startDate, endDate);
        return bookingRepository.findByStartTimeBetweenAndEndTimeBetween(startDate, endDate, startDate, endDate);
    }

    public List<Object[]> getBookingStatsByDayInRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Формирование статистики бронирований по дням с {} по {}", startDate, endDate);
        return bookingRepository.countBookingsByDay(startDate, endDate);
    }

    public List<Object[]> getTopBookedTablesInRange(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.debug("Получение топ-{} популярных столиков с {} по {}", limit, startDate, endDate);
        return bookingRepository.findMostBookedTablesInTimeRange(startDate, endDate, limit);
    }

    public long getCountCancelledBookingsInRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Подсчет отмененных бронирований с {} по {}", startDate, endDate);
        return bookingRepository.countByStatusAndStartTimeBetween(BookingStatus.CANCELLED, startDate, endDate);
    }

    @Transactional
    public Booking createBooking(BookingDto bookingDto) {
        log.debug("Создание нового бронирования: {}", bookingDto);

        validateBookingTime(bookingDto.getStartTime(), bookingDto.getEndTime());

        BookingTable table = bookingTableRepository.findById(bookingDto.getTableId())
                .orElseThrow(() -> {
                    log.error("Столик с ID {} не найден", bookingDto.getTableId());
                    return new ResourceNotFoundException("Столик с ID " + bookingDto.getTableId() + " не найден");
                });

        if (hasConflict(table.getId(), bookingDto.getStartTime(), bookingDto.getEndTime(), null)) {
            log.warn("Конфликт при бронировании столика #{}", table.getTableNumber());
            throw new BookingConflictException("Столик #" + table.getTableNumber() + " уже забронирован на указанное время");
        }

        Booking booking = Booking.builder()
                .table(table)
                .startTime(bookingDto.getStartTime())
                .endTime(bookingDto.getEndTime())
                .customerName(bookingDto.getCustomerName())
                .customerEmail(bookingDto.getCustomerEmail())
                .customerPhone(bookingDto.getCustomerPhone())
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Создано бронирование с ID: {}", savedBooking.getId());

        try {
            emailService.sendBookingConfirmation(savedBooking);
            notificationService.sendBookingConfirmationSms(savedBooking);
            notificationService.sendAdminNotificationSms(savedBooking, adminPhone);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомлений для бронирования ID {}: {}", savedBooking.getId(), e.getMessage(), e);
        }

        return savedBooking;
    }

    @Transactional
    public Booking cancelBooking(Long id) {
        log.debug("Отмена бронирования с ID: {}", id);

        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CANCELLED);

        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Бронирование с ID {} отменено", id);

        try {
            emailService.sendBookingCancellation(cancelledBooking);
            notificationService.sendBookingCancellationSms(cancelledBooking);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомлений об отмене бронирования ID {}: {}", id, e.getMessage(), e);
        }

        return cancelledBooking;
    }

    @Transactional
    public Booking updateBooking(Long id, BookingDto bookingDto) {
        log.debug("Обновление бронирования с ID: {}", id);

        validateBookingTime(bookingDto.getStartTime(), bookingDto.getEndTime());

        Booking existingBooking = getBookingById(id);
        BookingTable table = bookingTableRepository.findById(bookingDto.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Столик с ID " + bookingDto.getTableId() + " не найден"));

        if (!existingBooking.getTable().getId().equals(table.getId())
                || !existingBooking.getStartTime().equals(bookingDto.getStartTime())
                || !existingBooking.getEndTime().equals(bookingDto.getEndTime())) {

            if (hasConflict(table.getId(), bookingDto.getStartTime(), bookingDto.getEndTime(), id)) {
                log.warn("Конфликт при обновлении бронирования столика #{}", table.getTableNumber());
                throw new BookingConflictException("Столик #" + table.getTableNumber() + " уже забронирован на указанное время");
            }
        }

        existingBooking.setTable(table);
        existingBooking.setStartTime(bookingDto.getStartTime());
        existingBooking.setEndTime(bookingDto.getEndTime());
        existingBooking.setCustomerName(bookingDto.getCustomerName());
        existingBooking.setCustomerEmail(bookingDto.getCustomerEmail());
        existingBooking.setCustomerPhone(bookingDto.getCustomerPhone());

        Booking updatedBooking = bookingRepository.save(existingBooking);
        log.info("Бронирование с ID {} обновлено", updatedBooking.getId());

        try {
            emailService.sendBookingUpdate(updatedBooking);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления об обновлении бронирования ID {}: {}", updatedBooking.getId(), e.getMessage(), e);
        }

        return updatedBooking;
    }

    public boolean hasConflict(Long tableId, LocalDateTime startTime, LocalDateTime endTime, Long excludeBookingId) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(tableId, startTime, endTime);
        if (excludeBookingId != null) {
            return conflicts.stream().anyMatch(b -> !b.getId().equals(excludeBookingId));
        }
        return !CollectionUtils.isEmpty(conflicts);
    }

    public List<BookingTable> findAvailableTables(Integer capacity, LocalDateTime startTime, LocalDateTime endTime) {
        validateBookingTime(startTime, endTime);
        log.debug("Поиск доступных столиков на {} человек с {} по {}", capacity, startTime, endTime);
        return bookingTableRepository.findAvailableTables(capacity, startTime, endTime);
    }

    @Transactional
    public void deleteBooking(Long id) {
        log.debug("Удаление бронирования с ID: {}", id);
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
        log.info("Бронирование с ID {} успешно удалено", id);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldBookings() {
        log.info("Запуск очистки старых бронирований");

        LocalDateTime threshold = LocalDateTime.now().minusWeeks(1);
        List<Booking> oldBookings = bookingRepository.findBookingsOlderThan(threshold);

        if (!CollectionUtils.isEmpty(oldBookings)) {
            bookingRepository.deleteAll(oldBookings);
            log.info("Удалено {} старых бронирований", oldBookings.size());
        } else {
            log.info("Старые бронирования отсутствуют для удаления");
        }
    }

    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
    public void sendDailyReminders() {
        log.info("Запуск отправки напоминаний о бронированиях");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        List<Booking> bookings = findConfirmedBookingsInDateRange(startOfDay, endOfDay);

        log.info("Найдено {} бронирований для отправки напоминаний", bookings.size());

        for (Booking booking : bookings) {
            try {
                notificationService.sendBookingReminderSms(booking);
            } catch (Exception e) {
                log.error("Ошибка при отправке напоминания для бронирования ID {}: {}", booking.getId(), e.getMessage(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> findConfirmedBookingsInDateRange(LocalDateTime startFrom, LocalDateTime startTo) {
        return bookingRepository.findByStatusAndStartTimeBetween(
                BookingStatus.CONFIRMED, startFrom, startTo
        );
    }

    private void validateBookingTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startTime == null || endTime == null) {
            throw new InvalidBookingException("Необходимо указать время начала и окончания бронирования");
        }
        if (startTime.isBefore(now)) {
            throw new InvalidBookingException("Время начала бронирования не может быть в прошлом");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidBookingException("Время окончания должно быть позже времени начала");
        }
        if (startTime.plusHours(8).isBefore(endTime)) {
            throw new InvalidBookingException("Бронирование не может длиться дольше 8 часов");
        }
    }
}
