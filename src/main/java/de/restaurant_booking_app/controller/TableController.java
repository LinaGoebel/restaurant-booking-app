package de.restaurant_booking_app.controller;


import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@Slf4j
public class TableController {

    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    /**
     * Получение всех столиков
     */
    @GetMapping
    public ResponseEntity<List<BookingTable>> getAllTables() {
        log.info("Запрос на получение всех столиков");
        return ResponseEntity.ok(tableService.getAllTables());
    }

    /**
     * Получение столика по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingTable> getTableById(@PathVariable Long id) {
        log.info("Запрос на получение столика с ID: {}", id);
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    /**
     * Получение VIP столиков
     */
    @GetMapping("/vip")
    public ResponseEntity<List<BookingTable>> getVipTables() {
        log.info("Запрос на получение VIP столиков");
        return ResponseEntity.ok(tableService.getVipTables());
    }

    /**
     * Получение столиков по минимальной вместимости
     */
    @GetMapping("/capacity/{capacity}")
    public ResponseEntity<List<BookingTable>> getTablesByCapacity(@PathVariable Integer capacity) {
        log.info("Запрос на получение столиков с вместимостью >= {}", capacity);
        return ResponseEntity.ok(tableService.getTablesByMinCapacity(capacity));
    }

    /**
     * Создание нового столика
     */
    @PostMapping
    public ResponseEntity<BookingTable> createTable(@RequestBody BookingTable table) {
        log.info("Запрос на создание нового столика: {}", table);
        BookingTable createdTable = tableService.createTable(table);
        return new ResponseEntity<>(createdTable, HttpStatus.CREATED);
    }

    /**
     * Обновление столика
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookingTable> updateTable(@PathVariable Long id,
                                                    @RequestBody BookingTable table) {
        log.info("Запрос на обновление столика с ID: {}", id);
        return ResponseEntity.ok(tableService.updateTable(id, table));
    }

    /**
     * Удаление столика
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        log.info("Запрос на удаление столика с ID: {}", id);
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
