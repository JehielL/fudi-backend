package com.BiteBooking.backend.service;

import com.BiteBooking.backend.model.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@bitebooking.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm");

    @Async
    public void sendBookingConfirmation(Booking booking) {
        if (!emailEnabled) {
            log.info("Email deshabilitado - saltando confirmacion para reserva {}", booking.getId());
            return;
        }
        try {
            String subject = "Reserva confirmada en " + booking.getRestaurant().getName();
            String html = buildConfirmationEmail(booking);
            sendHtmlEmail(booking.getUser().getEmail(), subject, html);
            log.info("Email de confirmacion enviado para reserva {}", booking.getId());
        } catch (Exception e) {
            log.error("Error enviando email de confirmacion: {}", e.getMessage());
        }
    }

    @Async
    public void sendNewBookingNotification(Booking booking) {
        if (!emailEnabled) return;
        try {
            String subject = "Reserva recibida - " + booking.getRestaurant().getName();
            String html = buildNewBookingEmail(booking);
            sendHtmlEmail(booking.getUser().getEmail(), subject, html);
            log.info("Email de nueva reserva enviado para reserva {}", booking.getId());
        } catch (Exception e) {
            log.error("Error enviando email de nueva reserva: {}", e.getMessage());
        }
    }

    @Async
    public void sendBookingReminder(Booking booking) {
        if (!emailEnabled) return;
        try {
            String subject = "Recordatorio: Reserva manana en " + booking.getRestaurant().getName();
            String html = buildReminderEmail(booking);
            sendHtmlEmail(booking.getUser().getEmail(), subject, html);
            log.info("Recordatorio enviado para reserva {}", booking.getId());
        } catch (Exception e) {
            log.error("Error enviando recordatorio: {}", e.getMessage());
        }
    }

    @Async
    public void sendCancellationNotification(Booking booking) {
        if (!emailEnabled) return;
        try {
            String subject = "Reserva cancelada - " + booking.getRestaurant().getName();
            String html = buildCancellationEmail(booking);
            sendHtmlEmail(booking.getUser().getEmail(), subject, html);
            log.info("Email de cancelacion enviado para reserva {}", booking.getId());
        } catch (Exception e) {
            log.error("Error enviando email de cancelacion: {}", e.getMessage());
        }
    }

    @Async
    public void sendRestaurantNotification(Booking booking) {
        if (!emailEnabled) return;
        String restaurantEmail = booking.getRestaurant().getOwner() != null ? 
                booking.getRestaurant().getOwner().getEmail() : null;
        if (restaurantEmail == null) {
            log.warn("Restaurante {} no tiene email de propietario", booking.getRestaurant().getId());
            return;
        }
        try {
            String subject = "Nueva reserva para " + booking.getRestaurant().getName();
            String html = buildRestaurantNotificationEmail(booking);
            sendHtmlEmail(restaurantEmail, subject, html);
            log.info("Notificacion enviada al restaurante para reserva {}", booking.getId());
        } catch (Exception e) {
            log.error("Error enviando notificacion al restaurante: {}", e.getMessage());
        }
    }

    private String buildConfirmationEmail(Booking booking) {
        return buildEmailTemplate("Reserva Confirmada", "Tu reserva ha sido confirmada.", booking,
            frontendUrl + "/mis-reservas", "Ver mis reservas");
    }

    private String buildNewBookingEmail(Booking booking) {
        return buildEmailTemplate("Reserva Recibida", "Hemos recibido tu reserva. Te confirmaremos pronto.", booking,
            frontendUrl + "/mis-reservas", "Ver mis reservas");
    }

    private String buildReminderEmail(Booking booking) {
        return buildEmailTemplate("Recordatorio", "Te recordamos que manana tienes una reserva.", booking,
            frontendUrl + "/mis-reservas", "Ver detalles");
    }

    private String buildCancellationEmail(Booking booking) {
        String reason = booking.getCancellationReason() != null ? 
            " Motivo: " + booking.getCancellationReason() : "";
        return buildEmailTemplate("Reserva Cancelada", "Tu reserva ha sido cancelada." + reason, booking,
            frontendUrl + "/restaurantes/" + booking.getRestaurant().getId(), "Reservar de nuevo");
    }

    private String buildRestaurantNotificationEmail(Booking booking) {
        String customerName = booking.getUser().getFirstName() + " " + 
            (booking.getUser().getLastName() != null ? booking.getUser().getLastName() : "");
        String phoneHtml = booking.getContactPhone() != null ? 
            "<p><strong>Telefono:</strong> " + booking.getContactPhone() + "</p>" : "";
        String obsHtml = booking.getObservations() != null ? 
            "<p><strong>Observaciones:</strong> " + booking.getObservations() + "</p>" : "";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>");
        sb.append("<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        sb.append("<div style='background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; text-align: center;'>");
        sb.append("<h1>Nueva Reserva</h1></div>");
        sb.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 0 0 10px 10px;'>");
        sb.append("<h3>Cliente</h3>");
        sb.append("<p><strong>Nombre:</strong> ").append(customerName).append("</p>");
        sb.append("<p><strong>Email:</strong> ").append(booking.getUser().getEmail()).append("</p>");
        sb.append(phoneHtml);
        sb.append("<h3>Detalles</h3>");
        sb.append("<p><strong>Fecha:</strong> ").append(booking.getBookingDate().format(DATE_FORMATTER)).append("</p>");
        sb.append("<p><strong>Hora:</strong> ").append(booking.getBookingTime().format(TIME_FORMATTER)).append("</p>");
        sb.append("<p><strong>Personas:</strong> ").append(booking.getNumPeople()).append("</p>");
        sb.append(obsHtml);
        sb.append("<div style='text-align: center; margin-top: 20px;'>");
        sb.append("<a href='").append(frontendUrl).append("/dashboard/reservas' style='background: #11998e; color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; display: inline-block;'>Gestionar reservas</a>");
        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String buildEmailTemplate(String title, String message, Booking booking, String actionUrl, String actionText) {
        String address = booking.getRestaurant().getAddress() != null ? 
            booking.getRestaurant().getAddress() : "No especificada";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>");
        sb.append("<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        sb.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; text-align: center;'>");
        sb.append("<h1>BiteBooking</h1>");
        sb.append("<p style='font-size: 18px;'>").append(title).append("</p></div>");
        sb.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 0 0 10px 10px;'>");
        sb.append("<p>").append(message).append("</p>");
        sb.append("<div style='background: white; padding: 15px; border-radius: 8px; margin: 20px 0;'>");
        sb.append("<p><strong>Restaurante:</strong> ").append(booking.getRestaurant().getName()).append("</p>");
        sb.append("<p><strong>Fecha:</strong> ").append(booking.getBookingDate().format(DATE_FORMATTER)).append("</p>");
        sb.append("<p><strong>Hora:</strong> ").append(booking.getBookingTime().format(TIME_FORMATTER)).append("</p>");
        sb.append("<p><strong>Personas:</strong> ").append(booking.getNumPeople()).append("</p>");
        sb.append("<p><strong>Direccion:</strong> ").append(address).append("</p>");
        sb.append("</div>");
        sb.append("<div style='text-align: center; margin-top: 20px;'>");
        sb.append("<a href='").append(actionUrl).append("' style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; display: inline-block;'>").append(actionText).append("</a>");
        sb.append("</div></div>");
        sb.append("<p style='text-align: center; color: #666; font-size: 12px; margin-top: 20px;'>2025 BiteBooking. Todos los derechos reservados.</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
