package com.BiteBooking.backend.service;

import com.BiteBooking.backend.model.Favorite;
import com.BiteBooking.backend.model.Restaurant;
import com.BiteBooking.backend.model.User;
import com.BiteBooking.backend.repository.FavoriteRepository;
import com.BiteBooking.backend.repository.RestaurantRepository;
import com.BiteBooking.backend.security.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Agregar restaurante a favoritos
     */
    public Favorite addFavorite(Long restaurantId) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("Usuario no autenticado"));

        // Verificar si ya existe
        if (favoriteRepository.existsByUserIdAndRestaurantId(currentUser.getId(), restaurantId)) {
            throw new IllegalArgumentException("El restaurante ya está en favoritos");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        Favorite favorite = new Favorite();
        favorite.setUser(currentUser);
        favorite.setRestaurant(restaurant);

        log.info("Usuario {} agregó restaurante {} a favoritos", currentUser.getId(), restaurantId);
        return favoriteRepository.save(favorite);
    }

    /**
     * Quitar restaurante de favoritos
     */
    public void removeFavorite(Long restaurantId) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("Usuario no autenticado"));

        favoriteRepository.deleteByUserIdAndRestaurantId(currentUser.getId(), restaurantId);
        log.info("Usuario {} quitó restaurante {} de favoritos", currentUser.getId(), restaurantId);
    }

    /**
     * Toggle favorito (agregar si no existe, quitar si existe)
     */
    public boolean toggleFavorite(Long restaurantId) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("Usuario no autenticado"));

        if (favoriteRepository.existsByUserIdAndRestaurantId(currentUser.getId(), restaurantId)) {
            favoriteRepository.deleteByUserIdAndRestaurantId(currentUser.getId(), restaurantId);
            log.info("Usuario {} quitó restaurante {} de favoritos", currentUser.getId(), restaurantId);
            return false; // Ya no es favorito
        } else {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

            Favorite favorite = new Favorite();
            favorite.setUser(currentUser);
            favorite.setRestaurant(restaurant);
            favoriteRepository.save(favorite);
            log.info("Usuario {} agregó restaurante {} a favoritos", currentUser.getId(), restaurantId);
            return true; // Ahora es favorito
        }
    }

    /**
     * Verificar si un restaurante es favorito
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long restaurantId) {
        return SecurityUtils.getCurrentUser()
                .map(user -> favoriteRepository.existsByUserIdAndRestaurantId(user.getId(), restaurantId))
                .orElse(false);
    }

    /**
     * Obtener todos los favoritos del usuario actual
     */
    @Transactional(readOnly = true)
    public List<Favorite> getMyFavorites() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new SecurityException("Usuario no autenticado"));

        return favoriteRepository.findByUserId(currentUser.getId());
    }

    /**
     * Obtener solo los restaurantes favoritos
     */
    @Transactional(readOnly = true)
    public List<Restaurant> getMyFavoriteRestaurants() {
        return getMyFavorites().stream()
                .map(Favorite::getRestaurant)
                .collect(Collectors.toList());
    }

    /**
     * Contar cuántos usuarios tienen un restaurante como favorito
     */
    @Transactional(readOnly = true)
    public Long countFavorites(Long restaurantId) {
        return favoriteRepository.countByRestaurantId(restaurantId);
    }

    /**
     * Obtener los IDs de restaurantes favoritos del usuario (útil para marcar en listas)
     */
    @Transactional(readOnly = true)
    public List<Long> getMyFavoriteRestaurantIds() {
        return getMyFavorites().stream()
                .map(f -> f.getRestaurant().getId())
                .collect(Collectors.toList());
    }
}
