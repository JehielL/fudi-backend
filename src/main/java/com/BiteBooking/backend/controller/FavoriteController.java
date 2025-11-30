package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.model.Favorite;
import com.BiteBooking.backend.model.Restaurant;
import com.BiteBooking.backend.service.FavoriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Obtener mis restaurantes favoritos
     * GET /favorites
     */
    @GetMapping
    public ResponseEntity<List<Favorite>> getMyFavorites() {
        try {
            List<Favorite> favorites = favoriteService.getMyFavorites();
            return ResponseEntity.ok(favorites);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Obtener solo los restaurantes (sin wrapper Favorite)
     * GET /favorites/restaurants
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<Restaurant>> getMyFavoriteRestaurants() {
        try {
            List<Restaurant> restaurants = favoriteService.getMyFavoriteRestaurants();
            return ResponseEntity.ok(restaurants);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Obtener solo los IDs (útil para marcar en listas)
     * GET /favorites/ids
     */
    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getMyFavoriteIds() {
        try {
            List<Long> ids = favoriteService.getMyFavoriteRestaurantIds();
            return ResponseEntity.ok(ids);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Verificar si un restaurante es favorito
     * GET /favorites/check/{restaurantId}
     */
    @GetMapping("/check/{restaurantId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(@PathVariable Long restaurantId) {
        boolean isFavorite = favoriteService.isFavorite(restaurantId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    /**
     * Agregar a favoritos
     * POST /favorites/{restaurantId}
     */
    @PostMapping("/{restaurantId}")
    public ResponseEntity<Favorite> addFavorite(@PathVariable Long restaurantId) {
        try {
            Favorite favorite = favoriteService.addFavorite(restaurantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Quitar de favoritos
     * DELETE /favorites/{restaurantId}
     */
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long restaurantId) {
        try {
            favoriteService.removeFavorite(restaurantId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Toggle favorito (agregar/quitar)
     * POST /favorites/{restaurantId}/toggle
     */
    @PostMapping("/{restaurantId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long restaurantId) {
        try {
            boolean isFavorite = favoriteService.toggleFavorite(restaurantId);
            return ResponseEntity.ok(Map.of(
                "isFavorite", isFavorite,
                "message", isFavorite ? "Agregado a favoritos" : "Eliminado de favoritos"
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Contar cuántos usuarios tienen un restaurante como favorito
     * GET /favorites/count/{restaurantId}
     */
    @GetMapping("/count/{restaurantId}")
    public ResponseEntity<Map<String, Long>> countFavorites(@PathVariable Long restaurantId) {
        Long count = favoriteService.countFavorites(restaurantId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
