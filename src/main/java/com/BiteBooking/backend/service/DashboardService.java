package com.BiteBooking.backend.service;

import com.BiteBooking.backend.dto.DashboardStats;
import com.BiteBooking.backend.exception.UnauthorizedException;
import com.BiteBooking.backend.model.*;
import com.BiteBooking.backend.repository.*;
import com.BiteBooking.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final RatingRepository ratingRepository;
    private final PromotionRepository promotionRepository;
    private final MenuRepository menuRepository;

    /**
     * Obtener estadísticas completas del dashboard para un restaurante
     */
    public DashboardStats getRestaurantDashboard(Long restaurantId) {
        validateRestaurantOwnership(restaurantId);
        
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        DashboardStats stats = new DashboardStats();
        
        // Reservas de hoy
        List<Booking> todayBookings = bookingRepository.findTodayBookings(restaurantId, today);
        stats.setTodayBookings(todayBookings.size());
        
        // Personas esperadas hoy
        Integer peopleToday = bookingRepository.sumPeopleForDate(restaurantId, today);
        stats.setTodayPeople(peopleToday != null ? peopleToday : 0);
        
        // Reservas pendientes
        List<Booking> pendingBookings = bookingRepository.findPendingByRestaurant(restaurantId);
        stats.setPendingBookings(pendingBookings.size());
        
        // Estadísticas del mes
        Long confirmedThisMonth = bookingRepository.countByRestaurantAndStatusAndPeriod(
            restaurantId, BookingStatus.CONFIRMED, startOfMonth, endOfMonth
        );
        Long completedThisMonth = bookingRepository.countByRestaurantAndStatusAndPeriod(
            restaurantId, BookingStatus.COMPLETED, startOfMonth, endOfMonth
        );
        Long cancelledThisMonth = bookingRepository.countByRestaurantAndStatusAndPeriod(
            restaurantId, BookingStatus.CANCELLED, startOfMonth, endOfMonth
        );
        Long noShowThisMonth = bookingRepository.countByRestaurantAndStatusAndPeriod(
            restaurantId, BookingStatus.NO_SHOW, startOfMonth, endOfMonth
        );
        
        stats.setMonthlyConfirmed(confirmedThisMonth != null ? confirmedThisMonth.intValue() : 0);
        stats.setMonthlyCompleted(completedThisMonth != null ? completedThisMonth.intValue() : 0);
        stats.setMonthlyCancelled(cancelledThisMonth != null ? cancelledThisMonth.intValue() : 0);
        stats.setMonthlyNoShow(noShowThisMonth != null ? noShowThisMonth.intValue() : 0);
        
        // Próximas reservas confirmadas
        List<Booking> upcomingBookings = bookingRepository.findUpcomingConfirmed(restaurantId, today);
        stats.setUpcomingBookings(upcomingBookings);
        
        // Promociones activas
        Long activePromotions = promotionRepository.countActivePromotionsByRestaurant(restaurantId, today);
        stats.setActivePromotions(activePromotions != null ? activePromotions.intValue() : 0);
        
        // Rating promedio (si tienes el campo en Restaurant)
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
        if (restaurant != null && restaurant.getAverageRating() != null) {
            stats.setAverageRating(restaurant.getAverageRating());
        }
        
        // Lista de reservas de hoy para mostrar
        stats.setTodayBookingsList(todayBookings);
        stats.setPendingBookingsList(pendingBookings);
        
        return stats;
    }

    /**
     * Obtener resumen rápido (para notificaciones/badges)
     */
    public DashboardStats getQuickSummary(Long restaurantId) {
        validateRestaurantOwnership(restaurantId);
        
        LocalDate today = LocalDate.now();
        DashboardStats stats = new DashboardStats();
        
        // Solo datos esenciales
        List<Booking> pendingBookings = bookingRepository.findPendingByRestaurant(restaurantId);
        stats.setPendingBookings(pendingBookings.size());
        
        List<Booking> todayBookings = bookingRepository.findTodayBookings(restaurantId, today);
        stats.setTodayBookings(todayBookings.size());
        
        Integer peopleToday = bookingRepository.sumPeopleForDate(restaurantId, today);
        stats.setTodayPeople(peopleToday != null ? peopleToday : 0);
        
        return stats;
    }

    /**
     * Obtener reservas de un rango de fechas (para calendario)
     */
    public List<Booking> getBookingsForDateRange(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        validateRestaurantOwnership(restaurantId);
        return bookingRepository.findByRestaurantIdAndBookingDateBetween(restaurantId, startDate, endDate);
    }

    // ==================== MÉTODOS PRIVADOS ====================

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
