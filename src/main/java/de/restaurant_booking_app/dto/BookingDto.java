package de.restaurant_booking_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;

    @NotNull(message = "ID столика обязателен")
    private Long tableId;

    @NotNull(message = "Время начала обязательно")
    @Future(message = "Время начала должно быть в будущем")
    private LocalDateTime startTime;

    @NotNull(message = "Время окончания обязательно")
    @Future(message = "Время окончания должно быть в будущем")
    private LocalDateTime endTime;

    @NotBlank(message = "Имя клиента обязательно")
    @Size(min = 2, max = 100, message = "Имя клиента должно содержать от 2 до 100 символов")
    private String customerName;

    @NotBlank(message = "Email клиента обязателен")
    @Email(message = "Неверный формат email")
    @Size(max = 100, message = "Email не может быть длиннее 100 символов")
    private String customerEmail;

    @Pattern(regexp = "^\\+?[0-9\\s()-]{0,20}$", message = "Неверный формат телефона")
    private String customerPhone;
}
