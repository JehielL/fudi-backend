package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.Promotion;
import com.BiteBooking.backend.model.PromotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // Por restaurante
    List<Promotion> findByRestaurantId(Long restaurantId);
    
    List<Promotion> findByRestaurantIdAndActiveTrue(Long restaurantId);

    // Promociones activas y válidas para un restaurante
    @Query("SELECT p FROM Promotion p WHERE p.restaurant.id = :restaurantId " +
           "AND p.active = true AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotionsByRestaurant(
        @Param("restaurantId") Long restaurantId,
        @Param("today") LocalDate today
    );

    // Todas las promociones activas (para mostrar en home)
    @Query("SELECT p FROM Promotion p WHERE p.active = true " +
           "AND p.startDate <= :today AND p.endDate >= :today " +
           "ORDER BY p.featured DESC, p.createdAt DESC")
    List<Promotion> findAllActivePromotions(@Param("today") LocalDate today);

    // Promociones destacadas
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.featured = true " +
           "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findFeaturedPromotions(@Param("today") LocalDate today);

    // Por tipo
    List<Promotion> findByType(PromotionType type);

    // Por código promocional
    Optional<Promotion> findByPromoCodeIgnoreCase(String promoCode);

    // Promociones por ciudad (a través del restaurante)
    @Query("SELECT p FROM Promotion p WHERE p.restaurant.city = :city " +
           "AND p.active = true AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotionsByCity(
        @Param("city") String city,
        @Param("today") LocalDate today
    );

    // Contar promociones activas de un restaurante
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.restaurant.id = :restaurantId " +
           "AND p.active = true AND p.startDate <= :today AND p.endDate >= :today")
    Long countActivePromotionsByRestaurant(
        @Param("restaurantId") Long restaurantId,
        @Param("today") LocalDate today
    );
}
