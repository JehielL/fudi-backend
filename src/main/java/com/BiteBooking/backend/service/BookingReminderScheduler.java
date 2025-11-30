package com.BiteBooking.backend.service;

import com.BiteBooking.backend.model.Booking;
import com.BiteBooking.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReminderScheduler {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    /**
     * Enviar recordatorios para reservas del día siguiente
     * Se ejecuta todos los días a las 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * *")  // 10:00 AM todos los días
    @Transactional
    public void sendDailyReminders() {
        log.info("Iniciando envío de recordatorios diarios...");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Booking> bookingsForReminder = bookingRepository.findBookingsForReminder(tomorrow);
        
        log.info("Encontradas {} reservas para recordatorio mañana", bookingsForReminder.size());
        
        int sent = 0;
        int failed = 0;
        
        for (Booking booking : bookingsForReminder) {
            try {
                emailService.sendBookingReminder(booking);
                booking.setReminderSent(true);
                bookingRepository.save(booking);
                sent++;
            } catch (Exception e) {
                log.error("Error enviando recordatorio para reserva {}: {}", 
                        booking.getId(), e.getMessage());
                failed++;
            }
        }
        
        log.info("Recordatorios enviados: {} exitosos, {} fallidos", sent, failed);
    }

    /**
     * Segundo envío de recordatorios a las 6:00 PM
     * Para asegurar que todos reciban el recordatorio
     */
    @Scheduled(cron = "0 0 18 * * *")  // 6:00 PM todos los días
    @Transactional
    public void sendEveningReminders() {
        log.info("Iniciando segundo envío de recordatorios...");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Booking> bookingsForReminder = bookingRepository.findBookingsForReminder(tomorrow);
        
        // Solo enviar a los que aún no han recibido
        int sent = 0;
        for (Booking booking : bookingsForReminder) {
            if (!booking.getReminderSent()) {
                try {
                    emailService.sendBookingReminder(booking);
                    booking.setReminderSent(true);
                    bookingRepository.save(booking);
                    sent++;
                } catch (Exception e) {
                    log.error("Error en segundo intento de recordatorio para reserva {}: {}", 
                            booking.getId(), e.getMessage());
                }
            }
        }
        
        log.info("Segundo intento de recordatorios: {} enviados", sent);
    }
}
