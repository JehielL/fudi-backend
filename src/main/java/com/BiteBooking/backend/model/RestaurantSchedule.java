package com.BiteBooking.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Configuración de horarios de un restaurante.
 * Define los días y horas de apertura, capacidad, etc.
 */
@Entity
@Table(name = "restaurant_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Restaurant restaurant;

    // Día de la semana (MONDAY, TUESDAY, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    // ¿Está abierto este día?
    @Column(nullable = false)
    private Boolean isOpen = true;

    // Horario de apertura y cierre
    private LocalTime openTime;
    private LocalTime closeTime;

    // Horario de almuerzo (opcional, si tiene servicio dividido)
    private LocalTime lunchStart;
    private LocalTime lunchEnd;

    // Horario de cena (opcional)
    private LocalTime dinnerStart;
    private LocalTime dinnerEnd;

    // Capacidad máxima total del restaurante
    private Integer maxCapacity;

    // Capacidad máxima por slot de tiempo (ej: máximo 20 personas cada 30 min)
    private Integer maxCapacityPerSlot;

    // Duración de cada reserva en minutos (ej: 90 min almuerzo, 120 min cena)
    private Integer defaultBookingDurationMinutes = 90;

    // Intervalo de tiempo entre slots (15, 30, 60 minutos)
    private Integer slotIntervalMinutes = 30;

    // Tiempo mínimo de antelación para reservar (en horas)
    private Integer minAdvanceHours = 2;

    // Tiempo máximo de antelación para reservar (en días)
    private Integer maxAdvanceDays = 30;

    // ¿Acepta reservas online este día?
    private Boolean acceptsOnlineBookings = true;

    // Notas especiales para este día
    private String notes;

    /**
     * Verifica si una hora específica está dentro del horario de apertura
     */
    public boolean isTimeWithinOpenHours(LocalTime time) {
        if (!isOpen || openTime == null || closeTime == null) {
            return false;
        }
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    /**
     * Verifica si está en horario de almuerzo
     */
    public boolean isLunchTime(LocalTime time) {
        if (lunchStart == null || lunchEnd == null) {
            return false;
        }
        return !time.isBefore(lunchStart) && !time.isAfter(lunchEnd);
    }

    /**
     * Verifica si está en horario de cena
     */
    public boolean isDinnerTime(LocalTime time) {
        if (dinnerStart == null || dinnerEnd == null) {
            return false;
        }
        return !time.isBefore(dinnerStart) && !time.isAfter(dinnerEnd);
    }
}
