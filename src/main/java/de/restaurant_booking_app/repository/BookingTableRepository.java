package de.restaurant_booking_app.repository;

import de.restaurant_booking_app.model.BookingTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingTableRepository extends JpaRepository<BookingTable, Long> {

    Optional<BookingTable> findByTableNumber(Integer tableNumber);

    List<BookingTable> findByCapacityGreaterThanEqualOrderByCapacityAsc(Integer capacity);

    List<BookingTable> findByIsVip(Boolean isVip);

    // JPQL запрос для поиска доступных столиков
    @Query("SELECT t FROM BookingTable t WHERE t.capacity >= :capacity " +
            "AND t.id NOT IN (SELECT b.table.id FROM Booking b WHERE " +
            "b.status = 'CONFIRMED' AND " +
            "(b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<BookingTable> findAvailableTables(@Param("capacity") Integer capacity,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}


