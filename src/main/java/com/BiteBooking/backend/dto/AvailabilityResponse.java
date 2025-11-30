package com.BiteBooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para mostrar disponibilidad de un restaurante
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private Long restaurantId;
    private LocalDate date;
    private boolean isOpen;
    private String closedReason;
    private List<TimeSlotDTO> availableSlots;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotDTO {
        private LocalTime time;
        private int availableCapacity;
        private int maxCapacity;
        private boolean isAvailable;
        private String period;
    }
}
