package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.model.Promotion;
import com.BiteBooking.backend.service.PromotionService;
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
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    // ==================== PÚBLICOS (sin auth) ====================

    /**
     * Todas las promociones activas
     * GET /promotions
     */
    @GetMapping
    public ResponseEntity<List<Promotion>> getAllActivePromotions() {
        List<Promotion> promotions = promotionService.getAllActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    /**
     * Promociones destacadas (para home)
     * GET /promotions/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Promotion>> getFeaturedPromotions() {
        List<Promotion> promotions = promotionService.getFeaturedPromotions();
        return ResponseEntity.ok(promotions);
    }

    /**
     * Promociones activas de un restaurante
     * GET /promotions/restaurant/{restaurantId}
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Promotion>> getRestaurantPromotions(@PathVariable Long restaurantId) {
        List<Promotion> promotions = promotionService.getActivePromotionsByRestaurant(restaurantId);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Promociones por ciudad
     * GET /promotions/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Promotion>> getPromotionsByCity(@PathVariable String city) {
        List<Promotion> promotions = promotionService.getPromotionsByCity(city);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Detalle de una promoción
     * GET /promotions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getPromotion(@PathVariable Long id) {
        return promotionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Validar código promocional
     * GET /promotions/validate?code=ABC123&restaurantId=1
     */
    @GetMapping("/validate")
    public ResponseEntity<Promotion> validatePromoCode(
            @RequestParam String code,
            @RequestParam Long restaurantId
    ) {
        return promotionService.validatePromoCode(code, restaurantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== GESTIÓN (requiere auth) ====================

    /**
     * Todas las promociones de mi restaurante (incluye inactivas)
     * GET /promotions/restaurant/{restaurantId}/all
     */
    @GetMapping("/restaurant/{restaurantId}/all")
    public ResponseEntity<List<Promotion>> getAllRestaurantPromotions(@PathVariable Long restaurantId) {
        try {
            List<Promotion> promotions = promotionService.getAllPromotionsByRestaurant(restaurantId);
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Crear promoción
     * POST /promotions/restaurant/{restaurantId}
     */
    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Promotion> createPromotion(
            @PathVariable Long restaurantId,
            @RequestBody Promotion promotion
    ) {
        try {
            Promotion created = promotionService.createPromotion(promotion, restaurantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar promoción
     * PUT /promotions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Promotion> updatePromotion(
            @PathVariable Long id,
            @RequestBody Promotion promotion
    ) {
        try {
            Promotion updated = promotionService.updatePromotion(id, promotion);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Eliminar promoción
     * DELETE /promotions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Activar/desactivar promoción
     * POST /promotions/{id}/toggle-active
     */
    @PostMapping("/{id}/toggle-active")
    public ResponseEntity<Promotion> toggleActive(@PathVariable Long id) {
        try {
            Promotion promotion = promotionService.toggleActive(id);
            return ResponseEntity.ok(promotion);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marcar/desmarcar como destacada
     * POST /promotions/{id}/toggle-featured
     */
    @PostMapping("/{id}/toggle-featured")
    public ResponseEntity<Promotion> toggleFeatured(@PathVariable Long id) {
        try {
            Promotion promotion = promotionService.toggleFeatured(id);
            return ResponseEntity.ok(promotion);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Aplicar promoción (usar un uso)
     * POST /promotions/{id}/apply
     */
    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyPromotion(@PathVariable Long id) {
        boolean success = promotionService.applyPromotion(id);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Promoción aplicada correctamente"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "La promoción no es válida o ha expirado"
            ));
        }
    }
}
