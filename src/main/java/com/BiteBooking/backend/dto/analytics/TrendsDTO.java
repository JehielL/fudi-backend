package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendsDTO {
    private List<DailyMetricDTO> dailyTrend;
    private List<HourlyMetricDTO> hourlyDistribution;
    private List<WeekdayMetricDTO> weekdayDistribution;
}
