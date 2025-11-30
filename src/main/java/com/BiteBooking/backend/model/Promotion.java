package com.BiteBooking.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;  // "Happy Hour", "2x1 Pizzas", etc.

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;

    // Valor del descuento (porcentaje o cantidad fija según el tipo)
    private BigDecimal discountValue;

    // Para promociones con precio fijo (ej: Menú del día a 12.99€)
    private BigDecimal fixedPrice;

    // Fechas de validez
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // Horario específico (para Happy Hour)
    private LocalTime startTime;
    private LocalTime endTime;

    // Días de la semana válidos (formato: "1,2,3,4,5" para Lun-Vie)
    private String validDays;

    // Condiciones
    private Integer minPeople;  // Mínimo de personas para aplicar
    private Integer maxUses;    // Máximo de usos totales
    private Integer currentUses = 0;  // Usos actuales

    // Código promocional (opcional)
    private String promoCode;

    // Estado
    private Boolean active = true;
    private Boolean featured = false;  // Promoción destacada

    // Imagen de la promoción
    private String imageUrl;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnoreProperties({"owner", "hibernateLazyInitializer", "handler"})
    private Restaurant restaurant;

    // Métodos helper
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.featured == null) this.featured = false;
        if (this.currentUses == null) this.currentUses = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si la promoción está actualmente válida
     */
    public boolean isCurrentlyValid() {
        if (!active) return false;
        
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate) || today.isAfter(endDate)) return false;
        
        // Verificar si hay límite de usos
        if (maxUses != null && currentUses >= maxUses) return false;
        
        // Verificar horario si es Happy Hour
        if (type == PromotionType.HAPPY_HOUR && startTime != null && endTime != null) {
            LocalTime now = LocalTime.now();
            if (now.isBefore(startTime) || now.isAfter(endTime)) return false;
        }
        
        return true;
    }

    /**
     * Incrementa el contador de usos
     */
    public void incrementUses() {
        this.currentUses = (this.currentUses == null ? 0 : this.currentUses) + 1;
    }
}
