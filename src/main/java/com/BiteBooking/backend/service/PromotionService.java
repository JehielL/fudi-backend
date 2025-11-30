package com.BiteBooking.backend.service;

import com.BiteBooking.backend.model.Promotion;
import com.BiteBooking.backend.model.Restaurant;
import com.BiteBooking.backend.repository.PromotionRepository;
import com.BiteBooking.backend.repository.RestaurantRepository;
import com.BiteBooking.backend.security.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final RestaurantRepository restaurantRepository;

    // ==================== CRUD ====================

    public Promotion createPromotion(Promotion promotion, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        // Verificar que el usuario actual es dueño del restaurante
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (!restaurant.getOwner().getId().equals(user.getId())) {
                throw new SecurityException("No tienes permiso para crear promociones en este restaurante");
            }
        });

        promotion.setRestaurant(restaurant);
        
        // Valores por defecto
        if (promotion.getStartDate() == null) {
            promotion.setStartDate(LocalDate.now());
        }
        if (promotion.getCurrentUses() == null) {
            promotion.setCurrentUses(0);
        }

        log.info("Creando promoción '{}' para restaurante {}", promotion.getTitle(), restaurantId);
        return promotionRepository.save(promotion);
    }

    public Promotion updatePromotion(Long promotionId, Promotion promotionData) {
        Promotion existing = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        // Verificar permisos
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (!existing.getRestaurant().getOwner().getId().equals(user.getId())) {
                throw new SecurityException("No tienes permiso para editar esta promoción");
            }
        });

        // Actualizar campos
        existing.setTitle(promotionData.getTitle());
        existing.setDescription(promotionData.getDescription());
        existing.setType(promotionData.getType());
        existing.setDiscountValue(promotionData.getDiscountValue());
        existing.setFixedPrice(promotionData.getFixedPrice());
        existing.setStartDate(promotionData.getStartDate());
        existing.setEndDate(promotionData.getEndDate());
        existing.setStartTime(promotionData.getStartTime());
        existing.setEndTime(promotionData.getEndTime());
        existing.setValidDays(promotionData.getValidDays());
        existing.setMinPeople(promotionData.getMinPeople());
        existing.setMaxUses(promotionData.getMaxUses());
        existing.setPromoCode(promotionData.getPromoCode());
        existing.setActive(promotionData.getActive());
        existing.setFeatured(promotionData.getFeatured());

        log.info("Actualizando promoción {}", promotionId);
        return promotionRepository.save(existing);
    }

    public void deletePromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        // Verificar permisos
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (!promotion.getRestaurant().getOwner().getId().equals(user.getId())) {
                throw new SecurityException("No tienes permiso para eliminar esta promoción");
            }
        });

        log.info("Eliminando promoción {}", promotionId);
        promotionRepository.delete(promotion);
    }

    public Optional<Promotion> findById(Long id) {
        return promotionRepository.findById(id);
    }

    // ==================== CONSULTAS ====================

    /**
     * Promociones activas de un restaurante
     */
    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotionsByRestaurant(Long restaurantId) {
        return promotionRepository.findActivePromotionsByRestaurant(restaurantId, LocalDate.now());
    }

    /**
     * Todas las promociones de un restaurante (para gestión)
     */
    @Transactional(readOnly = true)
    public List<Promotion> getAllPromotionsByRestaurant(Long restaurantId) {
        return promotionRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Promociones destacadas (para página principal)
     */
    @Transactional(readOnly = true)
    public List<Promotion> getFeaturedPromotions() {
        return promotionRepository.findFeaturedPromotions(LocalDate.now());
    }

    /**
     * Todas las promociones activas (para explorar)
     */
    @Transactional(readOnly = true)
    public List<Promotion> getAllActivePromotions() {
        return promotionRepository.findAllActivePromotions(LocalDate.now());
    }

    /**
     * Buscar promoción por código
     */
    @Transactional(readOnly = true)
    public Optional<Promotion> findByPromoCode(String code) {
        return promotionRepository.findByPromoCodeIgnoreCase(code);
    }

    /**
     * Promociones activas en una ciudad
     */
    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsByCity(String city) {
        return promotionRepository.findActivePromotionsByCity(city, LocalDate.now());
    }

    // ==================== ACCIONES ====================

    /**
     * Activar/desactivar promoción
     */
    public Promotion toggleActive(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        // Verificar permisos
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (!promotion.getRestaurant().getOwner().getId().equals(user.getId())) {
                throw new SecurityException("No tienes permiso para modificar esta promoción");
            }
        });

        promotion.setActive(!promotion.getActive());
        log.info("Promoción {} ahora está {}", promotionId, promotion.getActive() ? "activa" : "inactiva");
        return promotionRepository.save(promotion);
    }

    /**
     * Marcar/desmarcar como destacada
     */
    public Promotion toggleFeatured(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        // Verificar permisos
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (!promotion.getRestaurant().getOwner().getId().equals(user.getId())) {
                throw new SecurityException("No tienes permiso para modificar esta promoción");
            }
        });

        promotion.setFeatured(!promotion.getFeatured());
        log.info("Promoción {} ahora está {}", promotionId, promotion.getFeatured() ? "destacada" : "no destacada");
        return promotionRepository.save(promotion);
    }

    /**
     * Aplicar promoción (incrementar uso)
     */
    public boolean applyPromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        if (!promotion.isCurrentlyValid()) {
            log.warn("Intento de aplicar promoción inválida: {}", promotionId);
            return false;
        }

        promotion.incrementUses();
        promotionRepository.save(promotion);
        log.info("Promoción {} aplicada. Usos: {}/{}", 
                promotionId, promotion.getCurrentUses(), promotion.getMaxUses());
        return true;
    }

    /**
     * Validar código promocional
     */
    public Optional<Promotion> validatePromoCode(String code, Long restaurantId) {
        Optional<Promotion> optPromotion = promotionRepository.findByPromoCodeIgnoreCase(code);
        
        if (optPromotion.isEmpty()) {
            return Optional.empty();
        }

        Promotion promotion = optPromotion.get();
        
        // Verificar que es del restaurante correcto
        if (!promotion.getRestaurant().getId().equals(restaurantId)) {
            return Optional.empty();
        }

        // Verificar que está válida
        if (!promotion.isCurrentlyValid()) {
            return Optional.empty();
        }

        return Optional.of(promotion);
    }
}
