package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatesDTO {
    private double confirmationRate;   // % reservas confirmadas
    private double cancellationRate;   // % reservas canceladas
    private double noShowRate;         // % no-shows
    private double completionRate;     // % completadas
}
