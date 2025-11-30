package com.BiteBooking.backend.dto;

import com.BiteBooking.backend.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para estadísticas del dashboard del restaurante
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    // Estadísticas de hoy
    private Integer todayBookings = 0;
    private Integer todayPeople = 0;
    
    // Reservas pendientes (requieren acción)
    private Integer pendingBookings = 0;
    
    // Estadísticas del mes
    private Integer monthlyConfirmed = 0;
    private Integer monthlyCompleted = 0;
    private Integer monthlyCancelled = 0;
    private Integer monthlyNoShow = 0;
    
    // Promociones
    private Integer activePromotions = 0;
    
    // Rating
    private Double averageRating = 0.0;
    
    // Listas de reservas
    private List<Booking> todayBookingsList;
    private List<Booking> upcomingBookings;
    private List<Booking> pendingBookingsList;
    
    // Métricas calculadas
    public Double getCompletionRate() {
        int total = monthlyConfirmed + monthlyCompleted + monthlyCancelled + monthlyNoShow;
        if (total == 0) return 0.0;
        return (monthlyCompleted.doubleValue() / total) * 100;
    }
    
    public Double getCancellationRate() {
        int total = monthlyConfirmed + monthlyCompleted + monthlyCancelled + monthlyNoShow;
        if (total == 0) return 0.0;
        return (monthlyCancelled.doubleValue() / total) * 100;
    }
    
    public Double getNoShowRate() {
        int total = monthlyConfirmed + monthlyCompleted + monthlyCancelled + monthlyNoShow;
        if (total == 0) return 0.0;
        return (monthlyNoShow.doubleValue() / total) * 100;
    }
}
