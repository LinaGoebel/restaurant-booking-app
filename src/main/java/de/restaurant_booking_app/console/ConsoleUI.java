package de.restaurant_booking_app.console;

import de.restaurant_booking_app.dto.BookingDto;
import de.restaurant_booking_app.exception.BookingConflictException;
import de.restaurant_booking_app.model.Booking;
import de.restaurant_booking_app.model.BookingStatus;
import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.service.BookingService;
import de.restaurant_booking_app.service.EmailReceiverService;
import de.restaurant_booking_app.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

@Component
@Profile("!test")
public class ConsoleUI implements CommandLineRunner {

    private final BookingService bookingService;
    private final TableService tableService;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final EmailReceiverService emailReceiverService;

    @Autowired
    public ConsoleUI(BookingService bookingService, TableService tableService, EmailReceiverService emailReceiverService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
        this.emailReceiverService = emailReceiverService;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run(String... args) {
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Запуск консольного интерфейса для бронирования столиков...");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntInput();

            switch (choice) {
                case 1:
                    createBooking();
                    break;
                case 2:
                    viewAllBookings();
                    break;
                case 3:
                    cancelBooking();
                    break;
                case 4:
                    running = false;
                    break;
                case 5:
                    running = false;
                    break;
                default:
                    System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }
        }

        System.out.println("Выход из программы.");
    }


    private void printMenu() {
        System.out.println("\n=== Система бронирования столиков ===");
        System.out.println("1. Создать бронирование");
        System.out.println("2. Просмотреть все бронирования");
        System.out.println("3. Отменить бронирование");
        System.out.println("4. Проверить входящую почту");
        System.out.println("5. Выйти");
        System.out.print("Выберите опцию: ");
    }

    private int readIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void createBooking() {
        System.out.println("\n=== Создание бронирования ===");

        // Показать доступные столики
        List<BookingTable> allTables = tableService.getAllTables();
        if (allTables.isEmpty()) {
            System.out.println("Нет доступных столиков. Сначала добавьте столики в систему.");
            return;
        }

        System.out.println("Доступные столики:");
        for (BookingTable table : allTables) {
            System.out.printf("ID: %d, Номер: %d, Вместимость: %d, VIP: %s%n",
                    table.getId(), table.getTableNumber(), table.getCapacity(),
                    table.getIsVip() ? "Да" : "Нет");
        }

        // Собрать данные для бронирования
        System.out.print("Введите ID столика: ");
        Long tableId = (long) readIntInput();

        System.out.print("Введите дату и время начала (yyyy-MM-dd HH:mm): ");
        LocalDateTime startTime = readDateTime();
        if (startTime == null) return;

        System.out.print("Введите дату и время окончания (yyyy-MM-dd HH:mm): ");
        LocalDateTime endTime = readDateTime();
        if (endTime == null) return;

        System.out.print("Введите имя клиента: ");
        String customerName = scanner.nextLine();

        System.out.print("Введите email клиента: ");
        String customerEmail = scanner.nextLine();

        System.out.print("Введите телефон клиента (необязательно): ");
        String customerPhone = scanner.nextLine();

        // Создать DTO и попытаться сохранить бронирование
        BookingDto bookingDto = BookingDto.builder()
                .tableId(tableId)
                .startTime(startTime)
                .endTime(endTime)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .build();

        try {
            Booking booking = bookingService.createBooking(bookingDto);
            System.out.println("Бронирование успешно создано с ID: " + booking.getId());
        } catch (BookingConflictException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    private void viewAllBookings() {
        System.out.println("\n=== Все бронирования ===");
        List<Booking> bookings = bookingService.getAllBookings();

        if (bookings.isEmpty()) {
            System.out.println("Бронирований нет.");
            return;
        }

        for (Booking booking : bookings) {
            System.out.printf("ID: %d, Столик: %d, Клиент: %s, Статус: %s%n" +
                            "Время: с %s по %s%n",
                    booking.getId(), booking.getTable().getTableNumber(),
                    booking.getCustomerName(), booking.getStatus(),
                    booking.getStartTime().format(formatter),
                    booking.getEndTime().format(formatter));
            System.out.println("-------------------------");
        }
    }

    private void cancelBooking() {
        System.out.println("\n=== Отмена бронирования ===");
        System.out.print("Введите ID бронирования для отмены: ");

        long bookingId = readIntInput();

        try {
            Booking booking = bookingService.getBookingById(bookingId);

            if (booking.getStatus() == BookingStatus.CANCELLED) {
                System.out.println("Это бронирование уже отменено.");
                return;
            }

            bookingService.cancelBooking(bookingId);
            System.out.println("Бронирование успешно отменено.");
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    private LocalDateTime readDateTime() {
        try {
            String input = scanner.nextLine();
            return LocalDateTime.parse(input, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат даты и времени. Используйте формат: yyyy-MM-dd HH:mm");
            return null;
        }
    }

    private void checkEmails() {
        System.out.println("\n=== Проверка непрочитанных писем ===");

        List<String> unreadEmails = emailReceiverService.readUnreadEmails();

        if (unreadEmails.isEmpty()) {
            System.out.println("Непрочитанных писем нет.");
            return;
        }

        System.out.println("Найдено " + unreadEmails.size() + " непрочитанных писем:");

        for (int i = 0; i < unreadEmails.size(); i++) {
            System.out.println("\nПисьмо #" + (i + 1) + ":");
            System.out.println("----------------------------");
            System.out.println(unreadEmails.get(i));
            System.out.println("----------------------------");
        }
    }
}
