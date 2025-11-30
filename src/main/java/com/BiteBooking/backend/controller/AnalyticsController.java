package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.dto.analytics.*;
import com.BiteBooking.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for restaurant analytics and reporting.
 * Provides endpoints for the back-office dashboard.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ==================== DASHBOARD PRINCIPAL ====================

    /**
     * GET /api/analytics/restaurant/{restaurantId}/dashboard
     * Complete dashboard analytics for a restaurant.
     * 
     * @param restaurantId The restaurant ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return DashboardAnalyticsDTO with all metrics
     */
    @GetMapping("/restaurant/{restaurantId}/dashboard")
    public ResponseEntity<DashboardAnalyticsDTO> getDashboardAnalytics(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Default to last 30 days if not provided
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(29);

        DashboardAnalyticsDTO analytics = analyticsService.getDashboardAnalytics(restaurantId, start, end);
        return ResponseEntity.ok(analytics);
    }

    // ==================== QUICK STATS ====================

    /**
     * GET /api/analytics/restaurant/{restaurantId}/quick
     * Quick stats for dashboard header (today, week, month).
     * 
     * @param restaurantId The restaurant ID
     * @return QuickStatsDTO with today/week/month overview
     */
    @GetMapping("/restaurant/{restaurantId}/quick")
    public ResponseEntity<QuickStatsDTO> getQuickStats(@PathVariable Long restaurantId) {
        QuickStatsDTO quickStats = analyticsService.getQuickStats(restaurantId);
        return ResponseEntity.ok(quickStats);
    }

    // ==================== TRENDS ENDPOINTS ====================

    /**
     * GET /api/analytics/restaurant/{restaurantId}/daily
     * Daily booking metrics for charts.
     * 
     * @param restaurantId The restaurant ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return List of DailyMetricDTO
     */
    @GetMapping("/restaurant/{restaurantId}/daily")
    public ResponseEntity<List<DailyMetricDTO>> getDailyMetrics(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(29);

        List<DailyMetricDTO> dailyMetrics = analyticsService.getDailyMetrics(restaurantId, start, end);
        return ResponseEntity.ok(dailyMetrics);
    }

    /**
     * GET /api/analytics/restaurant/{restaurantId}/hourly
     * Hourly distribution for charts.
     * 
     * @param restaurantId The restaurant ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return List of HourlyMetricDTO (24 hours)
     */
    @GetMapping("/restaurant/{restaurantId}/hourly")
    public ResponseEntity<List<HourlyMetricDTO>> getHourlyDistribution(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(29);

        List<HourlyMetricDTO> hourlyDistribution = analyticsService.getHourlyDistribution(restaurantId, start, end);
        return ResponseEntity.ok(hourlyDistribution);
    }

    /**
     * GET /api/analytics/restaurant/{restaurantId}/weekday
     * Weekday distribution for charts.
     * 
     * @param restaurantId The restaurant ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return List of WeekdayMetricDTO (7 days, Monday to Sunday)
     */
    @GetMapping("/restaurant/{restaurantId}/weekday")
    public ResponseEntity<List<WeekdayMetricDTO>> getWeekdayDistribution(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(29);

        List<WeekdayMetricDTO> weekdayDistribution = analyticsService.getWeekdayDistribution(restaurantId, start, end);
        return ResponseEntity.ok(weekdayDistribution);
    }

    // ==================== PERIOD PRESETS ====================

    /**
     * GET /api/analytics/restaurant/{restaurantId}/today
     * Dashboard for today only.
     */
    @GetMapping("/restaurant/{restaurantId}/today")
    public ResponseEntity<DashboardAnalyticsDTO> getTodayAnalytics(@PathVariable Long restaurantId) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getDashboardAnalytics(restaurantId, today, today));
    }

    /**
     * GET /api/analytics/restaurant/{restaurantId}/week
     * Dashboard for last 7 days.
     */
    @GetMapping("/restaurant/{restaurantId}/week")
    public ResponseEntity<DashboardAnalyticsDTO> getWeekAnalytics(@PathVariable Long restaurantId) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getDashboardAnalytics(restaurantId, today.minusDays(6), today));
    }

    /**
     * GET /api/analytics/restaurant/{restaurantId}/month
     * Dashboard for last 30 days.
     */
    @GetMapping("/restaurant/{restaurantId}/month")
    public ResponseEntity<DashboardAnalyticsDTO> getMonthAnalytics(@PathVariable Long restaurantId) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getDashboardAnalytics(restaurantId, today.minusDays(29), today));
    }

    /**
     * GET /api/analytics/restaurant/{restaurantId}/quarter
     * Dashboard for last 90 days.
     */
    @GetMapping("/restaurant/{restaurantId}/quarter")
    public ResponseEntity<DashboardAnalyticsDTO> getQuarterAnalytics(@PathVariable Long restaurantId) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getDashboardAnalytics(restaurantId, today.minusDays(89), today));
    }
}
