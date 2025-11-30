package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.RatingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingImageRepository extends JpaRepository<RatingImage, Long> {
    List<RatingImage> findByRatingIdOrderByImageOrder(Long ratingId);
}