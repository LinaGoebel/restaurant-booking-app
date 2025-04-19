package de.restaurant_booking_app.repository;

import de.restaurant_booking_app.model.Booking;
import de.restaurant_booking_app.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByTableId(Long tableId);

    List<Booking> findByCustomerEmail(String customerEmail);
    // Поиск бронирований для сегодняшнего дня
    @Query("SELECT b FROM Booking b WHERE " +
            "FUNCTION('YEAR', b.startTime) = FUNCTION('YEAR', CURRENT_DATE) AND " +
            "FUNCTION('MONTH', b.startTime) = FUNCTION('MONTH', CURRENT_DATE) AND " +
            "FUNCTION('DAY', b.startTime) = FUNCTION('DAY', CURRENT_DATE) AND " +
            "b.status = 'CONFIRMED'")
    List<Booking> findTodayBookings();

    // Проверка на конфликты при бронировании
    @Query("SELECT b FROM Booking b WHERE b.table.id = :tableId AND b.status = 'CONFIRMED' " +
            "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findConflictingBookings(@Param("tableId") Long tableId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    // Поиск старых бронирований
    @Query("SELECT b FROM Booking b WHERE b.endTime < :date")
    List<Booking> findBookingsOlderThan(@Param("date") LocalDateTime date);
}

