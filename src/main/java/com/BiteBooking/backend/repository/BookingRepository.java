package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.Booking;
import com.BiteBooking.backend.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Básicos
    List<Booking> findAllByRestaurant_Id(Long id);
    List<Booking> findAllByUserId(Long id);

    // Por estado
    List<Booking> findByRestaurantIdAndStatus(Long restaurantId, BookingStatus status);
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    // Reservas de hoy para un restaurante
    @Query("SELECT b FROM Booking b WHERE b.restaurant.id = :restaurantId " +
           "AND b.bookingDate = :date ORDER BY b.bookingTime ASC")
    List<Booking> findTodayBookings(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date
    );

    // Reservas pendientes de un restaurante
    @Query("SELECT b FROM Booking b WHERE b.restaurant.id = :restaurantId " +
           "AND b.status = 'PENDING' ORDER BY b.bookingDate ASC, b.bookingTime ASC")
    List<Booking> findPendingByRestaurant(@Param("restaurantId") Long restaurantId);

    // Próximas reservas confirmadas de un restaurante
    @Query("SELECT b FROM Booking b WHERE b.restaurant.id = :restaurantId " +
           "AND b.status = 'CONFIRMED' AND b.bookingDate >= :today " +
           "ORDER BY b.bookingDate ASC, b.bookingTime ASC")
    List<Booking> findUpcomingConfirmed(
        @Param("restaurantId") Long restaurantId,
        @Param("today") LocalDate today
    );

    // Historial de reservas del usuario (pasadas)
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
           "AND b.bookingDate < :today ORDER BY b.bookingDate DESC")
    List<Booking> findUserPastBookings(
        @Param("userId") Long userId,
        @Param("today") LocalDate today
    );

    // Próximas reservas del usuario
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
           "AND b.bookingDate >= :today AND b.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY b.bookingDate ASC, b.bookingTime ASC")
    List<Booking> findUserUpcomingBookings(
        @Param("userId") Long userId,
        @Param("today") LocalDate today
    );

    // Estadísticas: contar por estado en un período
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.restaurant.id = :restaurantId " +
           "AND b.status = :status AND b.bookingDate BETWEEN :startDate AND :endDate")
    Long countByRestaurantAndStatusAndPeriod(
        @Param("restaurantId") Long restaurantId,
        @Param("status") BookingStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Total de personas esperadas hoy
    @Query("SELECT COALESCE(SUM(b.numPeople), 0) FROM Booking b " +
           "WHERE b.restaurant.id = :restaurantId AND b.bookingDate = :date " +
           "AND b.status IN ('PENDING', 'CONFIRMED')")
    Integer sumPeopleForDate(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date
    );

    // Verificar disponibilidad (reservas en un horario específico)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.restaurant.id = :restaurantId " +
           "AND b.bookingDate = :date AND b.bookingTime = :time " +
           "AND b.status IN ('PENDING', 'CONFIRMED')")
    Long countBookingsAtTime(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date,
        @Param("time") java.time.LocalTime time
    );

    // Reservas por rango de fechas
    List<Booking> findByRestaurantIdAndBookingDateBetween(
        Long restaurantId, LocalDate startDate, LocalDate endDate
    );

    // Contar personas reservadas para un slot específico
    @Query("SELECT COALESCE(SUM(b.numPeople), 0) FROM Booking b " +
           "WHERE b.restaurant.id = :restaurantId AND b.bookingDate = :date " +
           "AND b.bookingTime = :time AND b.status IN ('PENDING', 'CONFIRMED')")
    Integer countBookedPeopleForSlot(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date,
        @Param("time") java.time.LocalTime time
    );

    // Buscar reservas para recordatorio (24h antes)
    @Query("SELECT b FROM Booking b WHERE b.bookingDate = :reminderDate " +
           "AND b.status = 'CONFIRMED' AND b.reminderSent = false")
    List<Booking> findBookingsForReminder(@Param("reminderDate") LocalDate reminderDate);

    // Buscar reservas confirmadas para una fecha
    @Query("SELECT b FROM Booking b WHERE b.restaurant.id = :restaurantId " +
           "AND b.bookingDate = :date AND b.status = 'CONFIRMED' " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findConfirmedBookingsForDate(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date
    );

    // =====================================================
    // ANALYTICS QUERIES - Para AnalyticsService
    // =====================================================

    // Contar reservas en un período
    @Query(value = "SELECT COUNT(*) FROM booking " +
           "WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countByRestaurantAndPeriod(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Sumar personas en un período
    @Query(value = "SELECT COALESCE(SUM(num_people), 0) FROM booking " +
           "WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long sumPeopleByRestaurantAndPeriod(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Contar por estado en período (usando String para status)
    @Query(value = "SELECT COUNT(*) FROM booking " +
           "WHERE restaurant_id = :restaurantId " +
           "AND status = :status " +
           "AND created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countByRestaurantAndStatusAndPeriod(
        @Param("restaurantId") Long restaurantId,
        @Param("status") String status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Clientes únicos
    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM booking " +
           "WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countDistinctCustomers(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Clientes recurrentes (más de 1 reserva)
    @Query(value = "SELECT COUNT(*) FROM (" +
           "SELECT user_id FROM booking " +
           "WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY user_id HAVING COUNT(*) > 1) as returning_customers", nativeQuery = true)
    Long countDistinctReturningCustomers(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Métricas diarias (para gráficos)
    @Query(value = "SELECT DATE(booking_date) as date, COUNT(*) as bookings, " +
           "COALESCE(SUM(num_people), 0) as guests " +
           "FROM booking WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(booking_date) ORDER BY date", nativeQuery = true)
    List<Object[]> getDailyMetrics(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Distribución por hora (para gráficos)
    @Query(value = "SELECT HOUR(booking_time) as hour, COUNT(*) as bookings, " +
           "AVG(num_people) as avg_guests " +
           "FROM booking WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY HOUR(booking_time) ORDER BY hour", nativeQuery = true)
    List<Object[]> getHourlyDistribution(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Distribución por día de semana (MySQL: 1=Sunday, 2=Monday...)
    @Query(value = "SELECT DAYOFWEEK(booking_date) as day_num, COUNT(*) as bookings, " +
           "AVG(num_people) as avg_guests " +
           "FROM booking WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY DAYOFWEEK(booking_date) ORDER BY day_num", nativeQuery = true)
    List<Object[]> getWeekdayDistribution(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Lead time promedio (días entre creación y fecha de reserva)
    @Query(value = "SELECT AVG(DATEDIFF(booking_date, DATE(created_at))) " +
           "FROM booking WHERE restaurant_id = :restaurantId " +
           "AND created_at BETWEEN :startDate AND :endDate " +
           "AND created_at IS NOT NULL", nativeQuery = true)
    Double getAverageLeadTimeDays(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}