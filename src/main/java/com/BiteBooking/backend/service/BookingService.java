package com.BiteBooking.backend.service;

import com.BiteBooking.backend.exception.UnauthorizedException;
import com.BiteBooking.backend.model.*;
import com.BiteBooking.backend.repository.BookingRepository;
import com.BiteBooking.backend.repository.RestaurantRepository;
import com.BiteBooking.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final AvailabilityService availabilityService;

    /**
     * Crear una nueva reserva
     */
    @Transactional
    public Booking createBooking(Booking booking) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Debe iniciar sesión para reservar"));
        
        booking.setUser(currentUser);
        booking.setStatus(BookingStatus.PENDING);
        
        // Validar que el restaurante existe
        if (booking.getRestaurant() == null || booking.getRestaurant().getId() == null) {
            throw new IllegalArgumentException("Debe especificar un restaurante");
        }
        
        Restaurant restaurant = restaurantRepository.findById(booking.getRestaurant().getId())
                .orElseThrow(() -> new NoSuchElementException("Restaurante no encontrado"));
        booking.setRestaurant(restaurant);
        
        // Validar fecha y hora
        if (booking.getBookingDate() == null || booking.getBookingTime() == null) {
            throw new IllegalArgumentException("Debe especificar fecha y hora de la reserva");
        }
        
        // Validar que la fecha no sea pasada
        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No puede reservar en una fecha pasada");
        }
        
        // Validar capacidad disponible (FASE 2A)
        int numPeople = booking.getNumPeople() != null ? booking.getNumPeople() : 1;
        String capacityError = availabilityService.getBookingErrorMessage(
            restaurant.getId(), 
            booking.getBookingDate(), 
            booking.getBookingTime(),
            numPeople
        );
        if (capacityError != null) {
            throw new IllegalArgumentException(capacityError);
        }
        
        // Inicializar campos de notificación
        booking.setReminderSent(false);
        booking.setConfirmationSent(false);
        
        log.info("Nueva reserva creada: {} personas para {} a las {}", 
                booking.getNumPeople(), booking.getBookingDate(), booking.getBookingTime());
        
        return bookingRepository.save(booking);
    }

    /**
     * Confirmar una reserva (solo dueño del restaurante o admin)
     */
    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = getBookingWithPermission(bookingId, true);
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar reservas pendientes");
        }
        
        booking.setStatus(BookingStatus.CONFIRMED);
        log.info("Reserva {} confirmada", bookingId);
        
        return bookingRepository.save(booking);
    }

    /**
     * Rechazar una reserva (solo dueño del restaurante o admin)
     */
    @Transactional
    public Booking rejectBooking(Long bookingId, String reason) {
        Booking booking = getBookingWithPermission(bookingId, true);
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden rechazar reservas pendientes");
        }
        
        booking.setStatus(BookingStatus.REJECTED);
        booking.setCancellationReason(reason);
        log.info("Reserva {} rechazada: {}", bookingId, reason);
        
        return bookingRepository.save(booking);
    }

    /**
     * Cancelar una reserva (usuario que la creó, dueño o admin)
     */
    @Transactional
    public Booking cancelBooking(Long bookingId, String reason) {
        Booking booking = getBookingWithPermission(bookingId, false);
        
        if (booking.getStatus() == BookingStatus.COMPLETED || 
            booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("No se puede cancelar esta reserva");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        log.info("Reserva {} cancelada: {}", bookingId, reason);
        
        return bookingRepository.save(booking);
    }

    /**
     * Marcar reserva como completada (cliente asistió)
     */
    @Transactional
    public Booking completeBooking(Long bookingId) {
        Booking booking = getBookingWithPermission(bookingId, true);
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Solo se pueden completar reservas confirmadas");
        }
        
        booking.setStatus(BookingStatus.COMPLETED);
        log.info("Reserva {} completada", bookingId);
        
        return bookingRepository.save(booking);
    }

    /**
     * Marcar como no-show (cliente no se presentó)
     */
    @Transactional
    public Booking markNoShow(Long bookingId) {
        Booking booking = getBookingWithPermission(bookingId, true);
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Solo se pueden marcar como no-show reservas confirmadas");
        }
        
        booking.setStatus(BookingStatus.NO_SHOW);
        log.info("Reserva {} marcada como no-show", bookingId);
        
        return bookingRepository.save(booking);
    }

    /**
     * Obtener reservas del usuario actual
     */
    public List<Booking> getMyBookings() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Debe iniciar sesión"));
        return bookingRepository.findAllByUserId(currentUser.getId());
    }

    /**
     * Obtener próximas reservas del usuario
     */
    public List<Booking> getMyUpcomingBookings() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Debe iniciar sesión"));
        return bookingRepository.findUserUpcomingBookings(currentUser.getId(), LocalDate.now());
    }

    /**
     * Obtener historial de reservas del usuario
     */
    public List<Booking> getMyPastBookings() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Debe iniciar sesión"));
        return bookingRepository.findUserPastBookings(currentUser.getId(), LocalDate.now());
    }

    /**
     * Obtener reservas de hoy para un restaurante
     */
    public List<Booking> getTodayBookings(Long restaurantId) {
        validateRestaurantOwnership(restaurantId);
        return bookingRepository.findTodayBookings(restaurantId, LocalDate.now());
    }

    /**
     * Obtener reservas pendientes de un restaurante
     */
    public List<Booking> getPendingBookings(Long restaurantId) {
        validateRestaurantOwnership(restaurantId);
        return bookingRepository.findPendingByRestaurant(restaurantId);
    }

    /**
     * Obtener próximas reservas confirmadas
     */
    public List<Booking> getUpcomingConfirmedBookings(Long restaurantId) {
        validateRestaurantOwnership(restaurantId);
        return bookingRepository.findUpcomingConfirmed(restaurantId, LocalDate.now());
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private Booking getBookingWithPermission(Long bookingId, boolean restaurantOnly) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada"));
        
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Debe iniciar sesión"));
        
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = booking.getRestaurant().getOwner() != null && 
                          booking.getRestaurant().getOwner().getId().equals(currentUser.getId());
        boolean isBookingUser = booking.getUser() != null && 
                                booking.getUser().getId().equals(currentUser.getId());
        
        if (restaurantOnly) {
            if (!isAdmin && !isOwner) {
                throw new UnauthorizedException("No tiene permisos para esta acción");
            }
        } else {
            if (!isAdmin && !isOwner && !isBookingUser) {
                throw new UnauthorizedException("No tiene permisos para esta acción");
            }
        }
        
        return booking;
    }

    private void validateRestaurantOwnership(Long restaurantId) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Debe iniciar sesión"));
        
        if (currentUser.getRole() != Role.ADMIN) {
            boolean isOwner = restaurantRepository.existsByOwner_IdAndId(currentUser.getId(), restaurantId);
            if (!isOwner) {
                throw new UnauthorizedException("No tiene permisos sobre este restaurante");
            }
        }
    }
}
