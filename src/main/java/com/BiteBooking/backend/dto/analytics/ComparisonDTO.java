package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonDTO {
    private double current;
    private double previous;
    private double changePercentage;
    private Trend trend;
    
    public enum Trend {
        UP, DOWN, STABLE
    }
}
