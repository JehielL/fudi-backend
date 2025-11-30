package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.dto.DashboardStats;
import com.BiteBooking.backend.model.Booking;
import com.BiteBooking.backend.service.DashboardService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Dashboard completo con todas las estadísticas del restaurante
     * GET /dashboard/restaurant/{restaurantId}
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<DashboardStats> getRestaurantDashboard(@PathVariable Long restaurantId) {
        try {
            DashboardStats stats = dashboardService.getRestaurantDashboard(restaurantId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error obteniendo dashboard para restaurante {}: {}", restaurantId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resumen rápido (para badges y notificaciones)
     * GET /dashboard/restaurant/{restaurantId}/quick
     * 
     * Respuesta:
     * {
     *   "todayBookings": 5,
     *   "pendingBookings": 3,
     *   "todayPeople": 12
     * }
     */
    @GetMapping("/restaurant/{restaurantId}/quick")
    public ResponseEntity<Map<String, Object>> getQuickSummary(@PathVariable Long restaurantId) {
        try {
            DashboardStats stats = dashboardService.getQuickSummary(restaurantId);
            Map<String, Object> summary = Map.of(
                "todayBookings", stats.getTodayBookings(),
                "pendingBookings", stats.getPendingBookings(),
                "todayPeople", stats.getTodayPeople()
            );
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error obteniendo resumen rápido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reservas por rango de fechas (para calendario)
     * GET /dashboard/restaurant/{restaurantId}/bookings?start=2024-01-01&end=2024-01-31
     */
    @GetMapping("/restaurant/{restaurantId}/bookings")
    public ResponseEntity<List<Booking>> getBookingsForDateRange(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        try {
            List<Booking> bookings = dashboardService.getBookingsForDateRange(restaurantId, start, end);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error obteniendo reservas por fecha: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Estadísticas del mes actual
     * GET /dashboard/restaurant/{restaurantId}/monthly
     */
    @GetMapping("/restaurant/{restaurantId}/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStats(@PathVariable Long restaurantId) {
        try {
            DashboardStats fullStats = dashboardService.getRestaurantDashboard(restaurantId);
            
            Map<String, Object> monthlyStats = Map.of(
                "confirmed", fullStats.getMonthlyConfirmed(),
                "completed", fullStats.getMonthlyCompleted(),
                "cancelled", fullStats.getMonthlyCancelled(),
                "noShow", fullStats.getMonthlyNoShow(),
                "completionRate", fullStats.getCompletionRate(),
                "cancellationRate", fullStats.getCancellationRate(),
                "noShowRate", fullStats.getNoShowRate()
            );
            
            return ResponseEntity.ok(monthlyStats);
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas mensuales: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reservas de hoy con detalles
     * GET /dashboard/restaurant/{restaurantId}/today
     */
    @GetMapping("/restaurant/{restaurantId}/today")
    public ResponseEntity<Map<String, Object>> getTodayDetails(@PathVariable Long restaurantId) {
        try {
            DashboardStats fullStats = dashboardService.getRestaurantDashboard(restaurantId);
            
            Map<String, Object> todayDetails = Map.of(
                "totalBookings", fullStats.getTodayBookings(),
                "totalPeople", fullStats.getTodayPeople(),
                "pending", fullStats.getPendingBookings(),
                "bookingsList", fullStats.getTodayBookingsList(),
                "pendingBookingsList", fullStats.getPendingBookingsList()
            );
            
            return ResponseEntity.ok(todayDetails);
        } catch (Exception e) {
            log.error("Error obteniendo detalles de hoy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
