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

    @Query("SELECT b FROM Booking b WHERE " +
            "FUNCTION('YEAR', b.startTime) = FUNCTION('YEAR', CURRENT_DATE) AND " +
            "FUNCTION('MONTH', b.startTime) = FUNCTION('MONTH', CURRENT_DATE) AND " +
            "FUNCTION('DAY', b.startTime) = FUNCTION('DAY', CURRENT_DATE) AND " +
            "b.status = 'CONFIRMED'")
    List<Booking> findTodayBookings();

    @Query("SELECT b FROM Booking b WHERE b.table.id = :tableId AND b.status = 'CONFIRMED' " +
            "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findConflictingBookings(@Param("tableId") Long tableId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Booking b WHERE b.endTime < :date")
    List<Booking> findBookingsOlderThan(@Param("date") LocalDateTime date);

    List<Booking> findByStatusAndStartTimeBetween(BookingStatus status, LocalDateTime startTime, LocalDateTime endTime);


    List<Booking> findByStartTimeBetweenAndEndTimeBetween(
            LocalDateTime startTimeFrom, LocalDateTime startTimeTo,
            LocalDateTime endTimeFrom, LocalDateTime endTimeTo);

    long countByStatusAndStartTimeBetween(BookingStatus status, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT FUNCTION('DATE', b.startTime) as bookingDate, COUNT(b.id) as bookingCount " +
            "FROM Booking b " +
            "WHERE b.startTime BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', b.startTime) " +
            "ORDER BY FUNCTION('DATE', b.startTime)")
    List<Object[]> countBookingsByDay(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT b.table_id as tableId, t.table_number as tableNumber, COUNT(b.id) as bookingCount " +
            "FROM bookings b " +
            "JOIN booking_table t ON b.table_id = t.id " +
            "WHERE b.start_time BETWEEN :startDate AND :endDate " +
            "GROUP BY b.table_id, t.table_number " +
            "ORDER BY bookingCount DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostBookedTablesInTimeRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit);
}

