package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.dto.AvailabilityResponse;
import com.BiteBooking.backend.exception.UnauthorizedException;
import com.BiteBooking.backend.model.*;
import com.BiteBooking.backend.repository.ClosedDateRepository;
import com.BiteBooking.backend.repository.RestaurantRepository;
import com.BiteBooking.backend.repository.RestaurantScheduleRepository;
import com.BiteBooking.backend.security.SecurityUtils;
import com.BiteBooking.backend.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class ScheduleController {

    private final RestaurantScheduleRepository scheduleRepository;
    private final ClosedDateRepository closedDateRepository;
    private final RestaurantRepository restaurantRepository;
    private final AvailabilityService availabilityService;

    // ==================== DISPONIBILIDAD (PÚBLICO) ====================

    /**
     * Obtener disponibilidad de un restaurante para una fecha
     * GET /api/restaurants/{id}/availability?date=2024-01-15&numPeople=4
     */
    @GetMapping("/restaurants/{restaurantId}/availability")
    public ResponseEntity<AvailabilityResponse> getAvailability(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "2") int numPeople) {
        
        AvailabilityResponse response = availabilityService.getAvailability(restaurantId, date, numPeople);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener disponibilidad para múltiples fechas
     * GET /api/restaurants/{id}/availability/week?startDate=2024-01-15&numPeople=4
     */
    @GetMapping("/restaurants/{restaurantId}/availability/week")
    public ResponseEntity<List<AvailabilityResponse>> getWeekAvailability(
            @PathVariable Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "2") int numPeople) {
        
        List<AvailabilityResponse> responses = new java.util.ArrayList<>();
        for (int i = 0; i < 7; i++) {
            responses.add(availabilityService.getAvailability(restaurantId, startDate.plusDays(i), numPeople));
        }
        return ResponseEntity.ok(responses);
    }

    // ==================== GESTIÓN DE HORARIOS (PROPIETARIOS) ====================

    /**
     * Obtener horarios configurados del restaurante
     * GET /api/restaurants/{id}/schedules
     */
    @GetMapping("/restaurants/{restaurantId}/schedules")
    public ResponseEntity<List<RestaurantSchedule>> getSchedules(@PathVariable Long restaurantId) {
        List<RestaurantSchedule> schedules = scheduleRepository.findByRestaurantId(restaurantId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Configurar horario para un día de la semana
     * POST /api/restaurants/{id}/schedules
     */
    @PostMapping("/restaurants/{restaurantId}/schedules")
    public ResponseEntity<RestaurantSchedule> createOrUpdateSchedule(
            @PathVariable Long restaurantId,
            @RequestBody RestaurantSchedule schedule) {
        
        validateRestaurantOwnership(restaurantId);
        Restaurant restaurant = getRestaurant(restaurantId);
        
        // Verificar si ya existe un horario para ese día
        scheduleRepository.findByRestaurantIdAndDayOfWeek(restaurantId, schedule.getDayOfWeek())
                .ifPresent(existing -> schedule.setId(existing.getId()));
        
        schedule.setRestaurant(restaurant);
        RestaurantSchedule saved = scheduleRepository.save(schedule);
        
        log.info("Horario actualizado para restaurante {} - {}", restaurantId, schedule.getDayOfWeek());
        return ResponseEntity.ok(saved);
    }

    /**
     * Configurar horarios para todos los días de la semana
     * PUT /api/restaurants/{id}/schedules/bulk
     */
    @PutMapping("/restaurants/{restaurantId}/schedules/bulk")
    public ResponseEntity<List<RestaurantSchedule>> bulkUpdateSchedules(
            @PathVariable Long restaurantId,
            @RequestBody List<RestaurantSchedule> schedules) {
        
        validateRestaurantOwnership(restaurantId);
        Restaurant restaurant = getRestaurant(restaurantId);
        
        List<RestaurantSchedule> saved = schedules.stream().map(schedule -> {
            scheduleRepository.findByRestaurantIdAndDayOfWeek(restaurantId, schedule.getDayOfWeek())
                    .ifPresent(existing -> schedule.setId(existing.getId()));
            schedule.setRestaurant(restaurant);
            return scheduleRepository.save(schedule);
        }).toList();
        
        log.info("Horarios bulk actualizados para restaurante {}", restaurantId);
        return ResponseEntity.ok(saved);
    }

    /**
     * Eliminar horario de un día
     * DELETE /api/restaurants/{id}/schedules/{scheduleId}
     */
    @DeleteMapping("/restaurants/{restaurantId}/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long restaurantId,
            @PathVariable Long scheduleId) {
        
        validateRestaurantOwnership(restaurantId);
        scheduleRepository.deleteById(scheduleId);
        return ResponseEntity.noContent().build();
    }

    // ==================== FECHAS CERRADAS ====================

    /**
     * Obtener fechas cerradas de un restaurante
     * GET /api/restaurants/{id}/closed-dates
     */
    @GetMapping("/restaurants/{restaurantId}/closed-dates")
    public ResponseEntity<List<ClosedDate>> getClosedDates(@PathVariable Long restaurantId) {
        List<ClosedDate> closedDates = closedDateRepository.findUpcomingClosedDates(
                restaurantId, LocalDate.now());
        return ResponseEntity.ok(closedDates);
    }

    /**
     * Agregar fecha cerrada
     * POST /api/restaurants/{id}/closed-dates
     */
    @PostMapping("/restaurants/{restaurantId}/closed-dates")
    public ResponseEntity<ClosedDate> addClosedDate(
            @PathVariable Long restaurantId,
            @RequestBody ClosedDate closedDate) {
        
        validateRestaurantOwnership(restaurantId);
        Restaurant restaurant = getRestaurant(restaurantId);
        
        closedDate.setRestaurant(restaurant);
        ClosedDate saved = closedDateRepository.save(closedDate);
        
        log.info("Fecha cerrada agregada para restaurante {}: {}", restaurantId, closedDate.getClosedDate());
        return ResponseEntity.ok(saved);
    }

    /**
     * Agregar múltiples fechas cerradas (ej: vacaciones)
     * POST /api/restaurants/{id}/closed-dates/bulk
     */
    @PostMapping("/restaurants/{restaurantId}/closed-dates/bulk")
    public ResponseEntity<List<ClosedDate>> addClosedDates(
            @PathVariable Long restaurantId,
            @RequestBody List<ClosedDate> closedDates) {
        
        validateRestaurantOwnership(restaurantId);
        Restaurant restaurant = getRestaurant(restaurantId);
        
        List<ClosedDate> saved = closedDates.stream().map(cd -> {
            cd.setRestaurant(restaurant);
            return closedDateRepository.save(cd);
        }).toList();
        
        log.info("Fechas cerradas bulk agregadas para restaurante {}: {} fechas", 
                restaurantId, closedDates.size());
        return ResponseEntity.ok(saved);
    }

    /**
     * Eliminar fecha cerrada
     * DELETE /api/restaurants/{id}/closed-dates/{closedDateId}
     */
    @DeleteMapping("/restaurants/{restaurantId}/closed-dates/{closedDateId}")
    public ResponseEntity<Void> removeClosedDate(
            @PathVariable Long restaurantId,
            @PathVariable Long closedDateId) {
        
        validateRestaurantOwnership(restaurantId);
        closedDateRepository.deleteById(closedDateId);
        return ResponseEntity.noContent().build();
    }

    // ==================== HELPERS ====================

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

    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Restaurante no encontrado"));
    }
}
