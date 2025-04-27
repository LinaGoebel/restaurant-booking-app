package de.restaurant_booking_app.controller;

import de.restaurant_booking_app.dto.BookingDto;
import de.restaurant_booking_app.model.Booking;
import de.restaurant_booking_app.model.BookingTable;
import de.restaurant_booking_app.service.BookingService;
import de.restaurant_booking_app.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    private final BookingService bookingService;
    private final TableService tableService;

    @Autowired
    public AdminController(BookingService bookingService, TableService tableService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
    }

    // Dashboard
    @GetMapping
    public String dashboard(Model model) {
        List<Booking> todayBookings = bookingService.getTodayBookings();
        model.addAttribute("todayBookings", todayBookings);
        return "admin/dashboard";
    }

    // Управление бронированиями
    @GetMapping("/bookings")
    public String bookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        List<Booking> bookings;

        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            // Предполагаем, что у BookingService есть метод getBookingsBetween
            bookings = bookingService.getBookingsByDateRange(start, end);
            model.addAttribute("selectedDate", date);
        } else {
            bookings = bookingService.getAllBookings();
        }

        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id);
        model.addAttribute("booking", booking);
        return "admin/booking-details";
    }

    @GetMapping("/bookings/create")
    public String createBookingForm(Model model) {
        model.addAttribute("bookingDto", new BookingDto());
        model.addAttribute("tables", tableService.getAllTables());
        return "admin/booking-form";
    }

    @PostMapping("/bookings/create")
    public String createBooking(@Valid @ModelAttribute BookingDto bookingDto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/booking-form";
        }

        try {
            Booking newBooking = bookingService.createBooking(bookingDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Бронирование #" + newBooking.getId() + " успешно создано");
            return "redirect:/admin/bookings/" + newBooking.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/bookings/create";
        }
    }

    @GetMapping("/bookings/{id}/edit")
    public String editBookingForm(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id);

        BookingDto bookingDto = BookingDto.builder()
                .id(booking.getId())
                .tableId(booking.getTable().getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .customerPhone(booking.getCustomerPhone())
                .build();

        model.addAttribute("bookingDto", bookingDto);
        model.addAttribute("tables", tableService.getAllTables());
        return "admin/booking-form";
    }

    @PostMapping("/bookings/{id}/edit")
    public String updateBooking(@PathVariable Long id,
                                @Valid @ModelAttribute BookingDto bookingDto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/booking-form";
        }

        try {
            bookingDto.setId(id); // Убедимся, что ID установлен
            Booking updatedBooking = bookingService.updateBooking(id, bookingDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Бронирование #" + updatedBooking.getId() + " успешно обновлено");
            return "redirect:/admin/bookings/" + updatedBooking.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/bookings/" + id + "/edit";
        }
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Бронирование #" + id + " успешно отменено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    // Управление столиками
    @GetMapping("/tables")
    public String tables(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        return "admin/tables";
    }

    @GetMapping("/tables/create")
    public String createTableForm(Model model) {
        model.addAttribute("table", new BookingTable());
        return "admin/table-form";
    }

    @PostMapping("/tables/create")
    public String createTable(@Valid @ModelAttribute("table") BookingTable table,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/table-form";
        }

        try {
            BookingTable newTable = tableService.createTable(table);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Столик #" + newTable.getTableNumber() + " успешно создан");
            return "redirect:/admin/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/tables/create";
        }
    }

    @GetMapping("/tables/{id}/edit")
    public String editTableForm(@PathVariable Long id, Model model) {
        BookingTable table = tableService.getTableById(id);
        model.addAttribute("table", table);
        return "admin/table-form";
    }

    @PostMapping("/tables/{id}/edit")
    public String updateTable(@PathVariable Long id,
                              @Valid @ModelAttribute("table") BookingTable table,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/table-form";
        }

        try {
            BookingTable updatedTable = tableService.updateTable(id, table);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Столик #" + updatedTable.getTableNumber() + " успешно обновлен");
            return "redirect:/admin/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/tables/" + id + "/edit";
        }
    }

    @PostMapping("/tables/{id}/delete")
    public String deleteTable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tableService.deleteTable(id);
            redirectAttributes.addFlashAttribute("successMessage", "Столик успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    // Отчеты
    @GetMapping("/reports")
    public String reports(Model model) {
        // Здесь можно добавить различные отчеты, например:
        // - Количество бронирований по дням/неделям/месяцам
        // - Загруженность столиков
        // - Статистика отмененных бронирований и т.д.
        return "admin/reports";
    }
}
