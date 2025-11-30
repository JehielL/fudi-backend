package com.BiteBooking.backend.model;


import com.BiteBooking.backend.config.BookingStatusConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "booking")
public class Booking {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    // Fecha y hora de la reserva (separados para facilitar búsquedas)
    private LocalDate bookingDate;
    private LocalTime bookingTime;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User user;

    private Integer numPeople;  // Renombrado de numUsers para claridad

    @Column(length = 500)
    private String observations;

    // Estado de la reserva - usa converter para manejar valores antiguos
    @Convert(converter = BookingStatusConverter.class)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    private Boolean interior;  // true = interior, false = terraza/exterior

    private Integer tableNumber;  // Renombrado de numTable

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @JsonIgnoreProperties({"owner", "hibernateLazyInitializer", "handler"})
    private Restaurant restaurant;

    private String specialRequests;  // Renombrado de extraService

    // Contacto adicional (por si reserva alguien para otro)
    private String contactName;
    private String contactPhone;

    // Motivo de cancelación/rechazo
    @Column(length = 500)
    private String cancellationReason;

    // Control de notificaciones
    @Column(nullable = false)
    private Boolean reminderSent = false;

    @Column(nullable = false)
    private Boolean confirmationSent = false;

    // Métodos helper
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor completo para datos de prueba
    public Booking(Long id, LocalDate bookingDate, LocalTime bookingTime, User user, 
                   Integer numPeople, String observations, BookingStatus status, 
                   Boolean interior, Integer tableNumber, Restaurant restaurant, 
                   String specialRequests) {
        this.id = id;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.user = user;
        this.numPeople = numPeople;
        this.observations = observations != null ? observations : "";
        this.status = status != null ? status : BookingStatus.PENDING;
        this.interior = interior != null ? interior : true;
        this.tableNumber = tableNumber;
        this.restaurant = restaurant;
        this.specialRequests = specialRequests != null ? specialRequests : "";
        this.reminderSent = false;
        this.confirmationSent = false;
    }
}
