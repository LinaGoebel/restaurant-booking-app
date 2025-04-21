package de.restaurant_booking_app.service;

import de.restaurant_booking_app.model.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Форматирование данных бронирования для отображения в письме
     */
    private String formatBookingDetails(Booking booking) {
        StringBuilder details = new StringBuilder();
        details.append("<p>Информация о вашем бронировании:</p>");
        details.append("<ul>");
        details.append("<li><strong>Номер бронирования:</strong> ").append(booking.getId()).append("</li>");
        details.append("<li><strong>Столик:</strong> ").append(booking.getTable().getTableNumber()).append("</li>");
        details.append("<li><strong>Дата и время начала:</strong> ").append(booking.getStartTime().format(formatter)).append("</li>");
        details.append("<li><strong>Дата и время окончания:</strong> ").append(booking.getEndTime().format(formatter)).append("</li>");
        details.append("<li><strong>Статус:</strong> ").append(getStatusText(booking.getStatus().toString())).append("</li>");
        details.append("</ul>");
        return details.toString();
    }

    /**
     * Перевод статуса на русский
     */
    private String getStatusText(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Подтверждено";
            case "CANCELLED" -> "Отменено";
            case "PENDING" -> "Ожидает подтверждения";
            default -> status;
        };
    }

    /**
     * Отправка письма с подтверждением бронирования
     */
    public void sendBookingConfirmation(Booking booking) {
        try {
            Context context = new Context();
            context.setVariable("name", booking.getCustomerName());
            context.setVariable("message", "Ваше бронирование успешно подтверждено.<br>" + formatBookingDetails(booking));

            sendEmail(booking.getCustomerEmail(), "Подтверждение бронирования столика", context);
            log.info("Отправлено подтверждение бронирования на email: {}", booking.getCustomerEmail());
        } catch (MessagingException e) {
            log.error("Ошибка при отправке подтверждения бронирования: {}", e.getMessage());
        }
    }

    /**
     * Отправка письма с отменой бронирования
     */
    public void sendBookingCancellation(Booking booking) {
        try {
            Context context = new Context();
            context.setVariable("name", booking.getCustomerName());
            context.setVariable("message", "Ваше бронирование было отменено.<br>" + formatBookingDetails(booking));

            sendEmail(booking.getCustomerEmail(), "Отмена бронирования столика", context);
            log.info("Отправлено уведомление об отмене бронирования на email: {}", booking.getCustomerEmail());
        } catch (MessagingException e) {
            log.error("Ошибка при отправке уведомления об отмене бронирования: {}", e.getMessage());
        }
    }

    /**
     * Отправка письма с обновлением бронирования
     */
    public void sendBookingUpdate(Booking booking) {
        try {
            Context context = new Context();
            context.setVariable("name", booking.getCustomerName());
            context.setVariable("message", "Ваше бронирование было изменено.<br>" + formatBookingDetails(booking));

            sendEmail(booking.getCustomerEmail(), "Изменение бронирования столика", context);
            log.info("Отправлено уведомление об изменении бронирования на email: {}", booking.getCustomerEmail());
        } catch (MessagingException e) {
            log.error("Ошибка при отправке уведомления об изменении бронирования: {}", e.getMessage());
        }
    }

    /**
     * Общий метод для отправки писем
     */
    private void sendEmail(String to, String subject, Context context) throws MessagingException {
        String htmlContent = templateEngine.process("email-template", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}