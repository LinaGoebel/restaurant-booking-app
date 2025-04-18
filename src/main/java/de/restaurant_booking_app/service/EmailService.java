package de.restaurant_booking_app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }




    /**
     * Отправка письма с подтверждением бронирования
     */
    public void sendBookingConfirmation(String email, String name, String bookingDetails) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("message", "Ваше бронирование успешно подтверждено:\n" + bookingDetails);

        sendEmail(email, "Подтверждение бронирования столика", context);
    }

    /**
     * Отправка письма с отменой бронирования
     */
    public void sendBookingCancellation(String email, String name, String bookingDetails) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("message", "Ваше бронирование было отменено:\n" + bookingDetails);

        sendEmail(email, "Отмена бронирования столика", context);
    }

    /**
     * Отправка письма с обновлением бронирования
     */
    public void sendBookingUpdate(String email, String name, String bookingDetails) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("message", "Ваше бронирование было изменено:\n" + bookingDetails);

        sendEmail(email, "Изменение бронирования столика", context);
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
        log.info("Отправлено письмо на адрес: {}, тема: {}", to, subject);
    }
}
