package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDTO {
    private int totalBookings;
    private int confirmedBookings;
    private int cancelledBookings;
    private int noShowBookings;
    private int totalGuests;
    private double avgPartySize;
}
