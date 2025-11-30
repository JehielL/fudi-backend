package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekdayMetricDTO {
    private int dayOfWeek;       // 1-7 (Sunday=1, Monday=2...)
    private String dayName;      // Lunes, Martes...
    private int bookings;
    private double avgGuests;
}
