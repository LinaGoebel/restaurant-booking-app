package de.restaurant_booking_app.console;

import de.restaurant_booking_app.dto.BookingDto;
import de.restaurant_booking_app.exception.BookingConflictException;
import de.restaurant_booking_app.model.Booking;
import de.restaurant_booking_app.model.BookingStatus;
import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.service.BookingService;
import de.restaurant_booking_app.service.EmailReceiverService;
import de.restaurant_booking_app.service.EmailService;
import de.restaurant_booking_app.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("!test")
public class ConsoleUI implements CommandLineRunner {

    private final BookingService bookingService;
    private final TableService tableService;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final EmailReceiverService emailReceiverService;
    private final EmailService emailService;

    @Autowired
    public ConsoleUI(BookingService bookingService, TableService tableService, EmailReceiverService emailReceiverService, EmailService emailService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
        this.emailReceiverService = emailReceiverService;
        this.scanner = new Scanner(System.in);
        this.emailService = emailService;
    }

    @Override
    public void run(String... args) {
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Запуск консольного интерфейса для бронирования столиков...");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = (int) readLongInput();

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
                    checkEmails();
                    break;
                case 5:
                    running = false;
                    break;
                case 6:
                    sendTestEmail();
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
        System.out.println("6. Отправить тестовое письмо администратору");
        System.out.print("Выберите опцию: ");
    }

    private long readLongInput() {
        try {
            return Long.parseLong(scanner.nextLine());
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
        Long tableId = readTableIdWithValidation();
        if (tableId == null) return;

        System.out.print("Введите дату и время начала (dd-MM-yyyy HH:mm): ");
        LocalDateTime startTime = readDateTime();
        if (startTime == null) return;

        System.out.print("Введите дату и время окончания (dd-MM-yyyy HH:mm): ");
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

        long bookingId = readLongInput();

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


    private static final List<String> EXIT_COMMANDS = Arrays.asList(
            "выход", "exit", "q", "quit", "отмена", "cancel"
    );

    private LocalDateTime readDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                String input = scanner.nextLine().trim();

                // Проверка на команды выхода
                if (EXIT_COMMANDS.contains(input.toLowerCase())) {
                    return null;
                }

                // Парсинг даты
                return LocalDateTime.parse(input, formatter);

            } catch (DateTimeParseException e) {
                System.out.println("\nОшибка: Неверный формат даты. Пример корректного формата: 31-12-2023 14:30");
                System.out.print("Пожалуйста, введите дату заново или одну из команд выхода ("
                        + String.join(", ", EXIT_COMMANDS) + "): ");
            }
        }
    }

    private void checkEmails() {
        System.out.println("\n=== Проверка непрочитанных писем ===");

        try {
            List<String> unreadEmails = emailReceiverService.readUnreadEmails();

            if (unreadEmails.isEmpty()) {
                System.out.println("Непрочитанных писем нет.");
                return; // Просто выходим из метода, но не из программы
            }

            System.out.println("Найдено " + unreadEmails.size() + " непрочитанных писем:");

            for (int i = 0; i < unreadEmails.size(); i++) {
                System.out.println("\nПисьмо #" + (i + 1) + ":");
                System.out.println("----------------------------");
                System.out.println(unreadEmails.get(i));
                System.out.println("----------------------------");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при проверке почты: " + e.getMessage());
            System.out.println("Трассировка стека: ");
            e.printStackTrace(); // Добавьте это для отладки
        }
    }

    private void sendTestEmail() {
        System.out.print("Введите email для отправки тестового письма администратору: ");
        String email = scanner.nextLine();

        try {
            Context context = new Context();
            context.setVariable("name", "Администратор");
            context.setVariable("message", "Это тестовое уведомление для администратора системы.");

            emailService.sendEmail(email, "Уведомление администратора", context);
            System.out.println("Тестовое письмо успешно отправлено на " + email);
        } catch (Exception e) {
            System.out.println("Ошибка при отправке письма: " + e.getMessage());
        }
    }

    private Long readTableIdWithValidation() {
        List<BookingTable> availableTables = tableService.getAllTables();
        Set<Long> existingTableIds = availableTables.stream()
                .map(BookingTable::getId)
                .collect(Collectors.toSet());

        while (true) {
            System.out.print("Введите ID столика: ");
            long inputId = readLongInput();

            if (inputId == -1) {
                System.out.println("Ошибка: Введите корректный числовой ID.");
                continue;
            }

            if (existingTableIds.contains(inputId)) {
                return inputId;
            } else {
                System.out.println("Ошибка: Столик с ID " + inputId + " не существует.");
                System.out.println("Доступные столики: " +
                        availableTables.stream()
                                .map(t -> t.getId().toString())
                                .collect(Collectors.joining(", ")));
            }
        }
    }

}
