package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.exception.FileException;
import com.BiteBooking.backend.exception.UnauthorizedException;
import com.BiteBooking.backend.model.Menu;
import com.BiteBooking.backend.model.Rating;
import com.BiteBooking.backend.model.RatingImage;
import com.BiteBooking.backend.model.RatingLike;
import com.BiteBooking.backend.model.Role;
import com.BiteBooking.backend.model.User;
import com.BiteBooking.backend.repository.MenuRepository;
import com.BiteBooking.backend.repository.RatingImageRepository;
import com.BiteBooking.backend.repository.RatingLikeRepository;
import com.BiteBooking.backend.repository.RatingRepository;
import com.BiteBooking.backend.security.SecurityUtils;
import com.BiteBooking.backend.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@Slf4j
@AllArgsConstructor
@CrossOrigin("*")
public class RatingController {

    private final RatingRepository ratingRepository;
    private final RatingImageRepository ratingImageRepository;
    private final RatingLikeRepository ratingLikeRepository;
    private final MenuRepository menuRepository;
    private final FileService fileService;

    // ==================== CRUD BÁSICO ====================

    @GetMapping("ratings/{id}")
    public Rating findById(@PathVariable Long id) {
        return this.ratingRepository.findById(id).orElseThrow();
    }

    @GetMapping("menus/filter-by-menu/{id}")
    public List<Rating> findAllByMenuId(@PathVariable Long id) {
        return this.ratingRepository.findByMenu_IdOrderByIdDesc(id);
    }

    @PostMapping(value = "ratings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Rating create(
            @RequestParam("score") Integer score,
            @RequestParam("comment") String comment,
            @RequestParam("menuId") Long menuId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        //  Validar máximo 3 imágenes
        if (images != null && images.size() > 3) {
            throw new FileException("No se pueden subir más de 3 imágenes por rating");
        }

        // Validar que todas sean imágenes
        if (images != null) {
            for (MultipartFile image : images) {
                if (!fileService.isImage(image)) {
                    throw new FileException("Solo se permiten archivos de imagen (jpg, jpeg, png, gif, webp)");
                }
            }
        }

        // Crear el rating
        Rating rating = new Rating();
        rating.setScore(score);
        rating.setComment(comment);
        rating.setLikesCount(0);

        // Obtener el menú
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NoSuchElementException("Menú no encontrado"));
        rating.setMenu(menu);

        // Obtener el usuario actual
        SecurityUtils.getCurrentUser().ifPresent(rating::setUser);

        // Guardar el rating primero para obtener el ID
        Rating savedRating = this.ratingRepository.save(rating);

        // Guardar las imágenes si existen
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);

                // Guardar el archivo
                String fileName = fileService.store(image);

                // Crear la entidad RatingImage
                RatingImage ratingImage = new RatingImage();
                ratingImage.setImagePath(fileName);
                ratingImage.setImageOrder(i + 1);
                ratingImage.setRating(savedRating);

                // Añadir a la colección existente (NO reemplazar)
                savedRating.getImages().add(ratingImage);
            }
            // Guardar nuevamente para persistir las imágenes
            savedRating = this.ratingRepository.save(savedRating);
        }

        return savedRating;
    }

    @PutMapping("ratings/{id}")
    public Rating update(@PathVariable Long id, @RequestBody Rating rating) {
        if (this.ratingRepository.existsById(id))
            return this.ratingRepository.save(rating);

        throw new NoSuchElementException();
    }

    @DeleteMapping("ratings/{id}")
    public void deleteById(@PathVariable Long id) {

        Rating rating = this.ratingRepository.findById(id).orElseThrow();
        User user = SecurityUtils.getCurrentUser().orElseThrow();

        if(user.getRole().equals(Role.ADMIN) ||
                (rating.getUser() != null && rating.getUser().getId().equals(user.getId()))
        ) {
            // Eliminar archivos físicos de las imágenes antes de borrar el rating
            if (rating.getImages() != null && !rating.getImages().isEmpty()) {
                for (RatingImage image : rating.getImages()) {
                    try {
                        fileService.delete(image.getImagePath());
                    } catch (Exception e) {
                        log.error("Error al eliminar imagen: {}", image.getImagePath(), e);
                    }
                }
            }
            
            this.ratingRepository.deleteById(id);
        }
        else
            throw new UnauthorizedException("No puede borrar el rating");

    }

    @GetMapping("ratings/{id}/images")
    public List<RatingImage> getRatingImages(@PathVariable Long id) {
        return this.ratingImageRepository.findByRatingIdOrderByImageOrder(id);
    }

    @DeleteMapping("ratings/{ratingId}/images/{imageId}")
    public void deleteRatingImage(@PathVariable Long ratingId, @PathVariable Long imageId) {
        Rating rating = this.ratingRepository.findById(ratingId).orElseThrow();
        User user = SecurityUtils.getCurrentUser().orElseThrow();

        // Verificar que el usuario sea el dueño del rating o admin
        if(user.getRole().equals(Role.ADMIN) ||
                (rating.getUser() != null && rating.getUser().getId().equals(user.getId()))
        ) {
            RatingImage image = this.ratingImageRepository.findById(imageId)
                    .orElseThrow(() -> new NoSuchElementException("Imagen no encontrada"));
            
            // Eliminar archivo físico
            try {
                fileService.delete(image.getImagePath());
            } catch (Exception e) {
                log.error("Error al eliminar archivo de imagen: {}", image.getImagePath(), e);
            }
            
            // Eliminar de la base de datos
            this.ratingImageRepository.deleteById(imageId);
        } else {
            throw new UnauthorizedException("No puede eliminar esta imagen");
        }
    }

    // ==================== LIKES ====================

    /**
     * Toggle like (dar o quitar según estado actual)
     * POST /ratings/{ratingId}/toggle-like
     */
    @PostMapping("ratings/{ratingId}/toggle-like")
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NoSuchElementException("Rating no encontrado"));
        
        User user = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Debe iniciar sesión para dar like"));

        Map<String, Object> response = new HashMap<>();

        // Verificar si ya dio like
        if (ratingLikeRepository.existsByRatingIdAndUserId(ratingId, user.getId())) {
            // Quitar like
            ratingLikeRepository.deleteByRatingIdAndUserId(ratingId, user.getId());
            rating.decrementLikes();
            ratingRepository.save(rating);
            
            response.put("message", "Like eliminado");
            response.put("liked", false);
        } else {
            // Dar like
            RatingLike like = new RatingLike(rating, user);
            ratingLikeRepository.save(like);
            rating.incrementLikes();
            ratingRepository.save(rating);
            
            response.put("message", "Like agregado");
            response.put("liked", true);
        }

        response.put("likesCount", rating.getLikesCount());
        return ResponseEntity.ok(response);
    }

    /**
     * Verificar si el usuario actual dio like a un rating
     * GET /ratings/{ratingId}/liked
     */
    @GetMapping("ratings/{ratingId}/liked")
    public ResponseEntity<Map<String, Object>> hasCurrentUserLiked(@PathVariable Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NoSuchElementException("Rating no encontrado"));

        Map<String, Object> response = new HashMap<>();
        response.put("likesCount", rating.getLikesCount());

        // Verificar si el usuario está autenticado
        User user = SecurityUtils.getCurrentUser().orElse(null);
        if (user != null) {
            boolean liked = ratingLikeRepository.existsByRatingIdAndUserId(ratingId, user.getId());
            response.put("liked", liked);
        } else {
            response.put("liked", false);
        }

        return ResponseEntity.ok(response);
    }

    // ==================== ORDENACIÓN POR LIKES ====================

    /**
     * Obtener todos los ratings ordenados por likes (mayor a menor)
     * GET /ratings/top
     */
    @GetMapping("ratings/top")
    public ResponseEntity<List<Rating>> getTopRatings() {
        return ResponseEntity.ok(ratingRepository.findAllByOrderByLikesCountDesc());
    }

    /**
     * Obtener ratings de un menú ordenados por likes
     * GET /ratings/menu/{menuId}/top
     */
    @GetMapping("ratings/menu/{menuId}/top")
    public ResponseEntity<List<Rating>> getMenuTopRatings(@PathVariable Long menuId) {
        return ResponseEntity.ok(ratingRepository.findByMenuIdOrderByLikesCountDesc(menuId));
    }

    /**
     * Obtener ratings de un menú ordenados por score
     * GET /ratings/menu/{menuId}/best
     */
    @GetMapping("ratings/menu/{menuId}/best")
    public ResponseEntity<List<Rating>> getMenuBestRatings(@PathVariable Long menuId) {
        return ResponseEntity.ok(ratingRepository.findByMenuIdOrderByScoreDesc(menuId));
    }

    /**
     * Obtener ratings con más de X likes (populares)
     * GET /ratings/popular?minLikes=5
     */
    @GetMapping("ratings/popular")
    public ResponseEntity<List<Rating>> getPopularRatings(
            @RequestParam(defaultValue = "5") Integer minLikes
    ) {
        return ResponseEntity.ok(
            ratingRepository.findByLikesCountGreaterThanOrderByLikesCountDesc(minLikes)
        );
    }

    /**
     * Obtener ratings de un usuario
     * GET /ratings/user/{userId}
     */
    @GetMapping("ratings/user/{userId}")
    public ResponseEntity<List<Rating>> getUserRatings(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingRepository.findByUserId(userId));
    }
}
