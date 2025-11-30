package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Favoritos de un usuario
    List<Favorite> findByUserId(Long userId);

    // Verificar si un restaurante es favorito del usuario
    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    // Obtener favorito específico
    Optional<Favorite> findByUserIdAndRestaurantId(Long userId, Long restaurantId);

    // Eliminar favorito
    void deleteByUserIdAndRestaurantId(Long userId, Long restaurantId);

    // Contar favoritos de un restaurante (popularidad)
    Long countByRestaurantId(Long restaurantId);

    // Restaurantes más populares (con más favoritos)
    // Se usará con query nativa o en el servicio
}
