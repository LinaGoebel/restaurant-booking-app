package de.restaurant_booking_app.config;

import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.repository.BookingTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class AppInit {

    @Bean
    @Order(1)
    public CommandLineRunner initDatabase(BookingTableRepository tableRepository) {
        return args -> {
            // Проверяем, есть ли уже столики в базе
            if (tableRepository.count() == 0) {
                System.out.println("Инициализация столиков...");

                // Добавляем несколько столиков
                tableRepository.save(BookingTable.builder()
                        .tableNumber(1)
                        .capacity(2)
                        .isVip(false)
                        .build());

                tableRepository.save(BookingTable.builder()
                        .tableNumber(2)
                        .capacity(4)
                        .isVip(false)
                        .build());

                tableRepository.save(BookingTable.builder()
                        .tableNumber(3)
                        .capacity(6)
                        .isVip(false)
                        .build());

                tableRepository.save(BookingTable.builder()
                        .tableNumber(4)
                        .capacity(4)
                        .isVip(true)
                        .build());

                System.out.println("Инициализация завершена. Добавлено 4 столика.");
            }
        };
    }
}
