package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.RestaurantSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantScheduleRepository extends JpaRepository<RestaurantSchedule, Long> {

    /**
     * Obtener todos los horarios de un restaurante
     */
    List<RestaurantSchedule> findByRestaurantId(Long restaurantId);

    /**
     * Obtener horario de un día específico
     */
    Optional<RestaurantSchedule> findByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    /**
     * Verificar si existe configuración para un restaurante
     */
    boolean existsByRestaurantId(Long restaurantId);

    /**
     * Obtener días abiertos de un restaurante
     */
    @Query("SELECT s FROM RestaurantSchedule s WHERE s.restaurant.id = :restaurantId AND s.isOpen = true")
    List<RestaurantSchedule> findOpenDaysByRestaurantId(Long restaurantId);

    /**
     * Eliminar todos los horarios de un restaurante
     */
    void deleteByRestaurantId(Long restaurantId);
}
