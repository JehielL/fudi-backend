package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickStatsDTO {
    private int todayBookings;
    private int todayGuests;
    private int weekBookings;
    private int weekGuests;
    private int monthBookings;
    private int monthGuests;
    private int pendingCount;
}
