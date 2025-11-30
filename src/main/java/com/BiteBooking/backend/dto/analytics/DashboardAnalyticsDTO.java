package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsDTO {
    private PeriodDTO period;
    private SummaryDTO summary;
    private StatusMetricsDTO statusBreakdown;
    private RatesDTO rates;
    private TrendsDTO trends;
    private InsightsDTO insights;
    private Map<String, ComparisonDTO> comparisons;
}
