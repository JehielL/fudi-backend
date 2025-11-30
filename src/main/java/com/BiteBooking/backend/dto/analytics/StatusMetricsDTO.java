package com.BiteBooking.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusMetricsDTO {
    private int pending;
    private int confirmed;
    private int completed;
    private int cancelled;
    private int noShow;
    private int rejected;
    
    public int getTotal() {
        return pending + confirmed + completed + cancelled + noShow + rejected;
    }
}
