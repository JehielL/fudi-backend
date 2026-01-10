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

    private LocalDate bookingDate;

    private LocalTime bookingTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User user;

    private Integer numPeople;

    @Column(length = 500)
    private String observations;

    @Convert(converter = BookingStatusConverter.class)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    private Boolean interior;

    private Integer tableNumber;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @JsonIgnoreProperties({"owner", "hibernateLazyInitializer", "handler"})
    private Restaurant restaurant;

    private String specialRequests;

    private String contactName;

    private String contactPhone;

    @Column(length = 500)
    private String cancellationReason;

    @Column(nullable = false)
    private Boolean reminderSent = false;

    @Column(nullable = false)
    private Boolean confirmationSent = false;
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

    // Constructor
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
