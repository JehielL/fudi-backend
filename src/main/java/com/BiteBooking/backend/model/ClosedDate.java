package com.BiteBooking.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Fechas cerradas del restaurante (festivos, vacaciones, días especiales)
 */
@Entity
@Table(name = "closed_date")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosedDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Restaurant restaurant;

    // Fecha cerrada
    @Column(nullable = false)
    private LocalDate closedDate;

    // Motivo del cierre
    private String reason;

    // ¿Es recurrente anualmente? (ej: Navidad)
    private Boolean isRecurringYearly = false;
}
