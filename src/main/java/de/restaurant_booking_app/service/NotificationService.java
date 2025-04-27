package de.restaurant_booking_app.service;

import de.restaurant_booking_app.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Value("${sms.api.url:}")
    private String smsApiUrl;

    @Value("${sms.api.key:}")
    private String smsApiKey;

    @Value("${push.api.url:}")
    private String pushApiUrl;

    @Value("${push.api.key:}")
    private String pushApiKey;

    @Value("${notification.enabled:false}")
    private boolean notificationEnabled;

    @Autowired
    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Отправка SMS о создании бронирования
     */
    public void sendBookingConfirmationSms(Booking booking) {
        if (!notificationEnabled || booking.getCustomerPhone() == null || booking.getCustomerPhone().isEmpty()) {
            log.debug("SMS-уведомление отключено или номер телефона не указан");
            return;
        }

        try {
            String message = String.format(
                    "Уважаемый(ая) %s, ваше бронирование #%d подтверждено. " +
                            "Столик: %d, Время: %s - %s. " +
                            "Ресторан \"У Клода\"",
                    booking.getCustomerName(),
                    booking.getId(),
                    booking.getTable().getTableNumber(),
                    booking.getStartTime().format(formatter),
                    booking.getEndTime().format(formatter)
            );

            sendSms(booking.getCustomerPhone(), message);
            log.info("SMS с подтверждением бронирования отправлено на номер: {}", booking.getCustomerPhone());
        } catch (Exception e) {
            log.error("Ошибка при отправке SMS с подтверждением бронирования: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправка SMS об отмене бронирования
     */
    public void sendBookingCancellationSms(Booking booking) {
        if (!notificationEnabled || booking.getCustomerPhone() == null || booking.getCustomerPhone().isEmpty()) {
            return;
        }

        try {
            String message = String.format(
                    "Уважаемый(ая) %s, ваше бронирование #%d отменено. " +
                            "Столик: %d, Время: %s - %s. " +
                            "По вопросам обращайтесь: +7 (999) 123-45-67. " +
                            "Ресторан \"У Клода\"",
                    booking.getCustomerName(),
                    booking.getId(),
                    booking.getTable().getTableNumber(),
                    booking.getStartTime().format(formatter),
                    booking.getEndTime().format(formatter)
            );

            sendSms(booking.getCustomerPhone(), message);
            log.info("SMS с отменой бронирования отправлено на номер: {}", booking.getCustomerPhone());
        } catch (Exception e) {
            log.error("Ошибка при отправке SMS с отменой бронирования: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправка SMS с напоминанием о бронировании (запускается по расписанию)
     */
    public void sendBookingReminderSms(Booking booking) {
        if (!notificationEnabled || booking.getCustomerPhone() == null || booking.getCustomerPhone().isEmpty()) {
            return;
        }

        try {
            String message = String.format(
                    "Уважаемый(ая) %s, напоминаем о вашем бронировании #%d сегодня в %s. " +
                            "Столик: %d. " +
                            "Ресторан \"У Клода\"",
                    booking.getCustomerName(),
                    booking.getId(),
                    booking.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    booking.getTable().getTableNumber()
            );

            sendSms(booking.getCustomerPhone(), message);
            log.info("SMS с напоминанием о бронировании отправлено на номер: {}", booking.getCustomerPhone());
        } catch (Exception e) {
            log.error("Ошибка при отправке SMS с напоминанием о бронировании: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправка SMS-уведомления администратору о новом бронировании
     */
    public void sendAdminNotificationSms(Booking booking, String adminPhone) {
        if (!notificationEnabled || adminPhone == null || adminPhone.isEmpty()) {
            return;
        }

        try {
            String message = String.format(
                    "Новое бронирование #%d. " +
                            "Клиент: %s, Тел: %s. " +
                            "Столик: %d, Время: %s - %s.",
                    booking.getId(),
                    booking.getCustomerName(),
                    booking.getCustomerPhone(),
                    booking.getTable().getTableNumber(),
                    booking.getStartTime().format(formatter),
                    booking.getEndTime().format(formatter)
            );

            sendSms(adminPhone, message);
            log.info("SMS с уведомлением администратору отправлено на номер: {}", adminPhone);
        } catch (Exception e) {
            log.error("Ошибка при отправке SMS с уведомлением администратору: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправка Push-уведомления
     */
    public void sendPushNotification(String userId, String title, String body) {
        if (!notificationEnabled) {
            return;
        }

        try {
            log.debug("Отправка push-уведомления пользователю {}: {} - {}", userId, title, body);

            // Пример вызова Push API (например, Firebase)
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", userId);

            Map<String, String> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", body);

            payload.put("notification", notification);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "key=" + pushApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(pushApiUrl, request, String.class);

            log.info("Push-уведомление успешно отправлено пользователю: {}", userId);
        } catch (Exception e) {
            log.error("Ошибка при отправке Push-уведомления: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправка SMS
     */
    private void sendSms(String phoneNumber, String message) {
        // Проверка параметров конфигурации
        if (smsApiUrl == null || smsApiUrl.isEmpty() || smsApiKey == null || smsApiKey.isEmpty()) {
            log.warn("SMS API не настроен. Сообщение не отправлено.");
            return;
        }

        try {
            // Формируем запрос к SMS шлюзу
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phone", phoneNumber);
            requestBody.put("message", message);
            requestBody.put("api_key", smsApiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Отправляем запрос к SMS API
            restTemplate.postForEntity(smsApiUrl, request, String.class);

            log.debug("SMS успешно отправлено на номер: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Ошибка при отправке SMS: {}", e.getMessage(), e);
            throw e;
        }
    }
}