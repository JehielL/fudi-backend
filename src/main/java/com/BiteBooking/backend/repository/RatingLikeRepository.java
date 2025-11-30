package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.RatingLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RatingLikeRepository extends JpaRepository<RatingLike, Long> {

    // Verificar si un usuario ya dio like a un rating
    boolean existsByRatingIdAndUserId(Long ratingId, Long userId);

    // Encontrar el like específico de un usuario en un rating
    Optional<RatingLike> findByRatingIdAndUserId(Long ratingId, Long userId);

    // Contar likes de un rating
    long countByRatingId(Long ratingId);

    // Eliminar like de un usuario en un rating
    @Transactional
    void deleteByRatingIdAndUserId(Long ratingId, Long userId);
}
