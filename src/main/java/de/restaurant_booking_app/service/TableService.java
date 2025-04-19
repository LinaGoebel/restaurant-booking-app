package de.restaurant_booking_app.service;

import de.restaurant_booking_app.exception.ResourceNotFoundException;
import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.repository.BookingTableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TableService {

    private final BookingTableRepository bookingTableRepository;

    @Autowired
    public TableService(BookingTableRepository bookingTableRepository) {
        this.bookingTableRepository = bookingTableRepository;
    }

    /**
     * Получение всех столиков
     */
    public List<BookingTable> getAllTables() {
        return bookingTableRepository.findAll();
    }

    /**
     * Получение столика по ID
     */
    public BookingTable getTableById(Long id) {
        return bookingTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Столик с ID " + id + " не найден"));
    }

    /**
     * Получение столика по номеру
     */
    public BookingTable getTableByNumber(Integer tableNumber) {
        return bookingTableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Столик с номером " + tableNumber + " не найден"));
    }

    /**
     * Получение VIP столиков
     */
    public List<BookingTable> getVipTables() {
        return bookingTableRepository.findByIsVip(true);
    }

    /**
     * Получение столиков с минимальной вместимостью
     */
    public List<BookingTable> getTablesByMinCapacity(Integer capacity) {
        return bookingTableRepository.findByCapacityGreaterThanEqualOrderByCapacityAsc(capacity);
    }

    /**
     * Создание нового столика
     */
    @Transactional
    public BookingTable createTable(BookingTable table) {
        // Проверяем, что номер столика уникален
        bookingTableRepository.findByTableNumber(table.getTableNumber()).ifPresent(existingTable -> {
            throw new IllegalArgumentException("Столик с номером " + table.getTableNumber() + " уже существует");
        });

        log.debug("Создание нового столика с номером: {}", table.getTableNumber());
        return bookingTableRepository.save(table);
    }

    /**
     * Обновление столика
     */
    @Transactional
    public BookingTable updateTable(Long id, BookingTable tableDetails) {
        BookingTable table = getTableById(id);

        // Проверяем, что новый номер столика уникален (если он изменен)
        if (!table.getTableNumber().equals(tableDetails.getTableNumber())) {
            bookingTableRepository.findByTableNumber(tableDetails.getTableNumber()).ifPresent(existingTable -> {
                throw new IllegalArgumentException("Столик с номером " + tableDetails.getTableNumber() + " уже существует");
            });
        }

        // Обновляем данные
        table.setTableNumber(tableDetails.getTableNumber());
        table.setCapacity(tableDetails.getCapacity());
        table.setIsVip(tableDetails.getIsVip());

        log.debug("Обновление столика с ID: {}", id);
        return bookingTableRepository.save(table);
    }

    /**
     * Удаление столика
     */
    @Transactional
    public void deleteTable(Long id) {
        BookingTable table = getTableById(id);
        log.debug("Удаление столика с ID: {}", id);
        bookingTableRepository.delete(table);
    }
}
