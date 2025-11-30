package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightsDTO {
    private Integer peakHour;           // 0-23
    private String peakDay;             // "Sábado"
    private double avgLeadTimeDays;     // días de anticipación promedio
    private double returningCustomerRate; // % clientes que vuelven
}
