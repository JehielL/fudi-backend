package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodName; // "Hoy", "Últimos 7 días", etc.
}
