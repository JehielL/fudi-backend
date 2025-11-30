package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Obtener ratings de un menú ordenados por ID (más recientes primero)
    List<Rating> findByMenu_IdOrderByIdDesc(Long id);

    // Obtener ratings de un menú ordenados por likes (mayor a menor)
    List<Rating> findByMenuIdOrderByLikesCountDesc(Long menuId);

    // Obtener ratings de un menú ordenados por score (mayor a menor)
    List<Rating> findByMenuIdOrderByScoreDesc(Long menuId);

    // Obtener todos los ratings ordenados por likes (mayor a menor)
    List<Rating> findAllByOrderByLikesCountDesc();

    // Obtener ratings de un usuario
    List<Rating> findByUserId(Long userId);

    // Buscar ratings con más de X likes
    List<Rating> findByLikesCountGreaterThanOrderByLikesCountDesc(Integer minLikes);

    @Transactional
    @Modifying
    void deleteByMenuId(Long menuId);
}

