package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.model.Booking;
import com.BiteBooking.backend.model.BookingStatus;
import com.BiteBooking.backend.repository.BookingRepository;
import com.BiteBooking.backend.security.SecurityUtils;
import com.BiteBooking.backend.service.BookingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    // ==================== CRUD BÁSICO ====================

    @GetMapping
    public List<Booking> findAll() {
        log.info("REST request to findAll Bookings");
        SecurityUtils.getCurrentUser().ifPresent(System.out::println);
        return this.bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> findById(@PathVariable Long id) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        return optionalBooking.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filter-by-restaurant/{id}")
    public List<Booking> findAllByRestaurantId(@PathVariable Long id) {
        return this.bookingRepository.findAllByRestaurant_Id(id);
    }

    @GetMapping("/filter-by-user/{id}")
    public List<Booking> findAllByUserId(@PathVariable Long id) {
        return this.bookingRepository.findAllByUserId(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Booking booking) {
        try {
            Booking created = bookingService.createBooking(booking);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear reserva: {}", e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al crear reserva: {}", e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Error al crear la reserva"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> update(@PathVariable Long id, @RequestBody Booking booking) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        booking.setId(id);
        Booking savedBooking = bookingRepository.save(booking);
        return ResponseEntity.ok(savedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        try {
            bookingRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar reserva {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GESTIÓN DE ESTADOS ====================

    /**
     * Confirmar una reserva (solo dueño del restaurante)
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingService.confirmBooking(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error al confirmar reserva: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Rechazar una reserva (solo dueño del restaurante)
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Booking> rejectBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String reason = body != null ? body.get("reason") : null;
            Booking booking = bookingService.rejectBooking(id, reason);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error al rechazar reserva: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancelar una reserva (usuario o dueño)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String reason = body != null ? body.get("reason") : null;
            Booking booking = bookingService.cancelBooking(id, reason);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error al cancelar reserva: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marcar reserva como completada (cliente asistió)
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Booking> completeBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingService.completeBooking(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error al completar reserva: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marcar como no-show (cliente no se presentó)
     */
    @PostMapping("/{id}/no-show")
    public ResponseEntity<Booking> markNoShow(@PathVariable Long id) {
        try {
            Booking booking = bookingService.markNoShow(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error al marcar no-show: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== MIS RESERVAS (USUARIO) ====================

    /**
     * Obtener todas mis reservas
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings() {
        try {
            List<Booking> bookings = bookingService.getMyBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Obtener mis próximas reservas
     */
    @GetMapping("/my-bookings/upcoming")
    public ResponseEntity<List<Booking>> getMyUpcomingBookings() {
        try {
            List<Booking> bookings = bookingService.getMyUpcomingBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Obtener mi historial de reservas
     */
    @GetMapping("/my-bookings/history")
    public ResponseEntity<List<Booking>> getMyPastBookings() {
        try {
            List<Booking> bookings = bookingService.getMyPastBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ==================== RESERVAS DEL RESTAURANTE ====================

    /**
     * Reservas de hoy de un restaurante
     */
    @GetMapping("/restaurant/{restaurantId}/today")
    public ResponseEntity<List<Booking>> getTodayBookings(@PathVariable Long restaurantId) {
        try {
            List<Booking> bookings = bookingService.getTodayBookings(restaurantId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Reservas pendientes de un restaurante
     */
    @GetMapping("/restaurant/{restaurantId}/pending")
    public ResponseEntity<List<Booking>> getPendingBookings(@PathVariable Long restaurantId) {
        try {
            List<Booking> bookings = bookingService.getPendingBookings(restaurantId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Próximas reservas confirmadas de un restaurante
     */
    @GetMapping("/restaurant/{restaurantId}/upcoming")
    public ResponseEntity<List<Booking>> getUpcomingBookings(@PathVariable Long restaurantId) {
        try {
            List<Booking> bookings = bookingService.getUpcomingConfirmedBookings(restaurantId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/restaurant/{restaurantId}/status/{status}")
    public ResponseEntity<List<Booking>> getByStatus(
            @PathVariable Long restaurantId,
            @PathVariable BookingStatus status
    ) {
        List<Booking> bookings = bookingRepository.findByRestaurantIdAndStatus(restaurantId, status);
        return ResponseEntity.ok(bookings);
    }
}




