package com.BiteBooking.backend.service;


import com.BiteBooking.backend.dto.analytics.*;
import com.BiteBooking.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for restaurant analytics and reporting.
 * Provides comprehensive metrics for the back-office dashboard.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final BookingRepository bookingRepository;

    // ==================== DASHBOARD PRINCIPAL ====================

    /**
     * Get complete dashboard analytics for a restaurant.
     * This is the main endpoint data for the back-office dashboard.
     */
    public DashboardAnalyticsDTO getDashboardAnalytics(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        // Define period
        PeriodDTO period = new PeriodDTO(startDate, endDate, getPeriodName(startDate, endDate));

        // Calculate all metrics
        SummaryDTO summary = calculateSummary(restaurantId, startDate, endDate);
        StatusMetricsDTO statusBreakdown = calculateStatusMetrics(restaurantId, startDate, endDate);
        RatesDTO rates = calculateRates(statusBreakdown, summary.getTotalBookings());
        TrendsDTO trends = calculateTrends(restaurantId, startDate, endDate);
        InsightsDTO insights = calculateInsights(restaurantId, startDate, endDate);

        // Calculate comparison with previous period
        Map<String, ComparisonDTO> comparisons = calculateComparisons(restaurantId, startDate, endDate);

        return new DashboardAnalyticsDTO(period, summary, statusBreakdown, rates, trends, insights, comparisons);
    }

    // ==================== QUICK STATS ====================

    /**
     * Get quick stats for the dashboard header.
     * Fast overview of today, this week, this month.
     */
    public QuickStatsDTO getQuickStats(Long restaurantId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = today.withDayOfMonth(1);

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        LocalDateTime weekStartDT = weekStart.atStartOfDay();
        LocalDateTime monthStartDT = monthStart.atStartOfDay();

        // Today stats
        Long todayBookings = bookingRepository.countByRestaurantAndPeriod(restaurantId, todayStart, todayEnd);
        Long todayGuests = Optional.ofNullable(
                bookingRepository.sumPeopleByRestaurantAndPeriod(restaurantId, todayStart, todayEnd)
        ).orElse(0L);

        // Week stats
        Long weekBookings = bookingRepository.countByRestaurantAndPeriod(restaurantId, weekStartDT, todayEnd);
        Long weekGuests = Optional.ofNullable(
                bookingRepository.sumPeopleByRestaurantAndPeriod(restaurantId, weekStartDT, todayEnd)
        ).orElse(0L);

        // Month stats
        Long monthBookings = bookingRepository.countByRestaurantAndPeriod(restaurantId, monthStartDT, todayEnd);
        Long monthGuests = Optional.ofNullable(
                bookingRepository.sumPeopleByRestaurantAndPeriod(restaurantId, monthStartDT, todayEnd)
        ).orElse(0L);

        // Pending count
        Long pendingCount = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "PENDING", todayStart, todayEnd.plusDays(30)
        );

        return new QuickStatsDTO(
                todayBookings.intValue(), todayGuests.intValue(),
                weekBookings.intValue(), weekGuests.intValue(),
                monthBookings.intValue(), monthGuests.intValue(),
                pendingCount.intValue()
        );
    }

    // ==================== SUMMARY CALCULATION ====================

    private SummaryDTO calculateSummary(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Long totalBookings = bookingRepository.countByRestaurantAndPeriod(restaurantId, start, end);
        Long confirmedBookings = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "CONFIRMED", start, end
        );
        Long cancelledBookings = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "CANCELLED", start, end
        );
        Long noShowBookings = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "NO_SHOW", start, end
        );
        Long totalGuests = Optional.ofNullable(
                bookingRepository.sumPeopleByRestaurantAndPeriod(restaurantId, start, end)
        ).orElse(0L);

        double avgPartySize = totalBookings > 0 
                ? roundToTwoDecimals((double) totalGuests / totalBookings) 
                : 0.0;

        return new SummaryDTO(
                totalBookings.intValue(),
                confirmedBookings.intValue(),
                cancelledBookings.intValue(),
                noShowBookings.intValue(),
                totalGuests.intValue(),
                avgPartySize
        );
    }

    // ==================== STATUS METRICS ====================

    private StatusMetricsDTO calculateStatusMetrics(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        int pending = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "PENDING", start, end
        ).intValue();
        int confirmed = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "CONFIRMED", start, end
        ).intValue();
        int completed = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "COMPLETED", start, end
        ).intValue();
        int cancelled = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "CANCELLED", start, end
        ).intValue();
        int noShow = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "NO_SHOW", start, end
        ).intValue();
        int rejected = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "REJECTED", start, end
        ).intValue();

        return new StatusMetricsDTO(pending, confirmed, completed, cancelled, noShow, rejected);
    }

    // ==================== RATES CALCULATION ====================

    private RatesDTO calculateRates(StatusMetricsDTO status, int totalBookings) {
        if (totalBookings == 0) {
            return new RatesDTO(0.0, 0.0, 0.0, 0.0);
        }

        double confirmationRate = roundToTwoDecimals(
                (double) (status.getConfirmed() + status.getCompleted()) / totalBookings * 100
        );
        double cancellationRate = roundToTwoDecimals(
                (double) status.getCancelled() / totalBookings * 100
        );
        double noShowRate = roundToTwoDecimals(
                (double) status.getNoShow() / totalBookings * 100
        );
        double completionRate = roundToTwoDecimals(
                (double) status.getCompleted() / totalBookings * 100
        );

        return new RatesDTO(confirmationRate, cancellationRate, noShowRate, completionRate);
    }

    // ==================== TRENDS ====================

    private TrendsDTO calculateTrends(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // Daily trend
        List<DailyMetricDTO> dailyTrend = processDailyMetrics(
                bookingRepository.getDailyMetrics(restaurantId, start, end)
        );

        // Hourly distribution
        List<HourlyMetricDTO> hourlyDistribution = processHourlyMetrics(
                bookingRepository.getHourlyDistribution(restaurantId, start, end)
        );

        // Weekday distribution
        List<WeekdayMetricDTO> weekdayDistribution = processWeekdayMetrics(
                bookingRepository.getWeekdayDistribution(restaurantId, start, end)
        );

        return new TrendsDTO(dailyTrend, hourlyDistribution, weekdayDistribution);
    }

    // ==================== INSIGHTS ====================

    private InsightsDTO calculateInsights(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // Peak hour
        List<Object[]> hourlyData = bookingRepository.getHourlyDistribution(restaurantId, start, end);
        Integer peakHour = findPeakHour(hourlyData);

        // Peak day
        List<Object[]> weekdayData = bookingRepository.getWeekdayDistribution(restaurantId, start, end);
        String peakDay = findPeakDay(weekdayData);

        // Average lead time
        Double avgLeadTime = Optional.ofNullable(
                bookingRepository.getAverageLeadTimeDays(restaurantId, start, end)
        ).orElse(0.0);

        // Returning customer rate
        Long totalCustomers = bookingRepository.countDistinctCustomers(restaurantId, start, end);
        Long returningCustomers = bookingRepository.countDistinctReturningCustomers(restaurantId, start, end);
        double returningRate = totalCustomers > 0 
                ? roundToTwoDecimals((double) returningCustomers / totalCustomers * 100)
                : 0.0;

        return new InsightsDTO(peakHour, peakDay, roundToTwoDecimals(avgLeadTime), returningRate);
    }

    // ==================== COMPARISONS ====================

    private Map<String, ComparisonDTO> calculateComparisons(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        Map<String, ComparisonDTO> comparisons = new HashMap<>();

        // Calculate previous period (same duration, immediately before)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevStart = startDate.minusDays(daysBetween);
        LocalDate prevEnd = startDate.minusDays(1);

        LocalDateTime currentStart = startDate.atStartOfDay();
        LocalDateTime currentEnd = endDate.atTime(23, 59, 59);
        LocalDateTime previousStart = prevStart.atStartOfDay();
        LocalDateTime previousEnd = prevEnd.atTime(23, 59, 59);

        // Bookings comparison
        Long currentBookings = bookingRepository.countByRestaurantAndPeriod(restaurantId, currentStart, currentEnd);
        Long previousBookings = bookingRepository.countByRestaurantAndPeriod(restaurantId, previousStart, previousEnd);
        comparisons.put("bookings", createComparison(currentBookings.doubleValue(), previousBookings.doubleValue()));

        // Guests comparison
        Long currentGuests = Optional.ofNullable(
                bookingRepository.sumPeopleByRestaurantAndPeriod(restaurantId, currentStart, currentEnd)
        ).orElse(0L);
        Long previousGuests = Optional.ofNullable(
                bookingRepository.sumPeopleByRestaurantAndPeriod(restaurantId, previousStart, previousEnd)
        ).orElse(0L);
        comparisons.put("guests", createComparison(currentGuests.doubleValue(), previousGuests.doubleValue()));

        // Cancellation rate comparison
        Long currentCancelled = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "CANCELLED", currentStart, currentEnd
        );
        Long previousCancelled = bookingRepository.countByRestaurantAndStatusAndPeriod(
                restaurantId, "CANCELLED", previousStart, previousEnd
        );
        double currentCancelRate = currentBookings > 0 ? (double) currentCancelled / currentBookings * 100 : 0;
        double previousCancelRate = previousBookings > 0 ? (double) previousCancelled / previousBookings * 100 : 0;
        comparisons.put("cancellationRate", createComparison(currentCancelRate, previousCancelRate));

        return comparisons;
    }

    private ComparisonDTO createComparison(double current, double previous) {
        double changePercentage = previous > 0 
                ? roundToTwoDecimals((current - previous) / previous * 100)
                : (current > 0 ? 100.0 : 0.0);
        
        ComparisonDTO.Trend trend;
        if (Math.abs(changePercentage) < 1.0) {
            trend = ComparisonDTO.Trend.STABLE;
        } else if (changePercentage > 0) {
            trend = ComparisonDTO.Trend.UP;
        } else {
            trend = ComparisonDTO.Trend.DOWN;
        }

        return new ComparisonDTO(current, previous, changePercentage, trend);
    }

    // ==================== DATA PROCESSING HELPERS ====================

    private List<DailyMetricDTO> processDailyMetrics(List<Object[]> data) {
        return data.stream()
                .map(row -> {
                    LocalDate date = row[0] instanceof java.sql.Date 
                            ? ((java.sql.Date) row[0]).toLocalDate()
                            : LocalDate.parse(row[0].toString());
                    int bookings = ((Number) row[1]).intValue();
                    int guests = ((Number) row[2]).intValue();
                    return new DailyMetricDTO(date, bookings, guests);
                })
                .sorted(Comparator.comparing(DailyMetricDTO::getDate))
                .collect(Collectors.toList());
    }

    private List<HourlyMetricDTO> processHourlyMetrics(List<Object[]> data) {
        // Initialize all 24 hours
        Map<Integer, HourlyMetricDTO> hourMap = new HashMap<>();
        for (int h = 0; h < 24; h++) {
            hourMap.put(h, new HourlyMetricDTO(h, 0, 0.0));
        }

        // Fill with actual data
        for (Object[] row : data) {
            int hour = ((Number) row[0]).intValue();
            int bookings = ((Number) row[1]).intValue();
            double avgGuests = ((Number) row[2]).doubleValue();
            hourMap.put(hour, new HourlyMetricDTO(hour, bookings, roundToTwoDecimals(avgGuests)));
        }

        return hourMap.values().stream()
                .sorted(Comparator.comparingInt(HourlyMetricDTO::getHour))
                .collect(Collectors.toList());
    }

    private List<WeekdayMetricDTO> processWeekdayMetrics(List<Object[]> data) {
        // Initialize all 7 days (MySQL DAYOFWEEK: 1=Sunday, 2=Monday, ..., 7=Saturday)
        Map<Integer, WeekdayMetricDTO> dayMap = new HashMap<>();
        String[] dayNames = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
        for (int d = 1; d <= 7; d++) {
            dayMap.put(d, new WeekdayMetricDTO(d, dayNames[d - 1], 0, 0.0));
        }

        // Fill with actual data
        for (Object[] row : data) {
            int dayOfWeek = ((Number) row[0]).intValue();
            int bookings = ((Number) row[1]).intValue();
            double avgGuests = ((Number) row[2]).doubleValue();
            dayMap.put(dayOfWeek, new WeekdayMetricDTO(dayOfWeek, dayNames[dayOfWeek - 1], bookings, roundToTwoDecimals(avgGuests)));
        }

        // Reorder to start from Monday (2,3,4,5,6,7,1)
        List<WeekdayMetricDTO> result = new ArrayList<>();
        for (int d = 2; d <= 7; d++) result.add(dayMap.get(d));
        result.add(dayMap.get(1)); // Sunday at end

        return result;
    }

    private Integer findPeakHour(List<Object[]> hourlyData) {
        return hourlyData.stream()
                .max(Comparator.comparingInt(row -> ((Number) row[1]).intValue()))
                .map(row -> ((Number) row[0]).intValue())
                .orElse(null);
    }

    private String findPeakDay(List<Object[]> weekdayData) {
        String[] dayNames = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
        return weekdayData.stream()
                .max(Comparator.comparingInt(row -> ((Number) row[1]).intValue()))
                .map(row -> dayNames[((Number) row[0]).intValue() - 1])
                .orElse(null);
    }

    // ==================== UTILITY METHODS ====================

    private String getPeriodName(LocalDate startDate, LocalDate endDate) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        if (days <= 1) return "Hoy";
        if (days <= 7) return "Últimos 7 días";
        if (days <= 14) return "Últimas 2 semanas";
        if (days <= 30) return "Último mes";
        if (days <= 90) return "Últimos 3 meses";
        
        return startDate.toString() + " - " + endDate.toString();
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // ==================== INDIVIDUAL ENDPOINT METHODS ====================

    /**
     * Get daily metrics for charts.
     */
    public List<DailyMetricDTO> getDailyMetrics(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return processDailyMetrics(bookingRepository.getDailyMetrics(restaurantId, start, end));
    }

    /**
     * Get hourly distribution for charts.
     */
    public List<HourlyMetricDTO> getHourlyDistribution(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return processHourlyMetrics(bookingRepository.getHourlyDistribution(restaurantId, start, end));
    }

    /**
     * Get weekday distribution for charts.
     */
    public List<WeekdayMetricDTO> getWeekdayDistribution(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return processWeekdayMetrics(bookingRepository.getWeekdayDistribution(restaurantId, start, end));
    }
}
