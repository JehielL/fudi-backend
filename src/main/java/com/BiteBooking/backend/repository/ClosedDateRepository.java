package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.ClosedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClosedDateRepository extends JpaRepository<ClosedDate, Long> {

    /**
     * Obtener todas las fechas cerradas de un restaurante
     */
    List<ClosedDate> findByRestaurantId(Long restaurantId);

    /**
     * Verificar si una fecha específica está cerrada
     */
    Optional<ClosedDate> findByRestaurantIdAndClosedDate(Long restaurantId, LocalDate closedDate);

    /**
     * Verificar si existe cierre para una fecha
     */
    boolean existsByRestaurantIdAndClosedDate(Long restaurantId, LocalDate closedDate);

    /**
     * Obtener fechas cerradas futuras
     */
    @Query("SELECT c FROM ClosedDate c WHERE c.restaurant.id = :restaurantId AND c.closedDate >= :fromDate ORDER BY c.closedDate")
    List<ClosedDate> findUpcomingClosedDates(Long restaurantId, LocalDate fromDate);

    /**
     * Obtener fechas cerradas en un rango
     */
    @Query("SELECT c FROM ClosedDate c WHERE c.restaurant.id = :restaurantId AND c.closedDate BETWEEN :startDate AND :endDate")
    List<ClosedDate> findByRestaurantIdAndDateRange(Long restaurantId, LocalDate startDate, LocalDate endDate);

    /**
     * Eliminar fecha cerrada
     */
    void deleteByRestaurantIdAndClosedDate(Long restaurantId, LocalDate closedDate);
}
