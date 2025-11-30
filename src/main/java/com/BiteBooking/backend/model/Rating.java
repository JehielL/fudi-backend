package com.BiteBooking.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer score;

    @Column(length = 1000)
    private String comment;

    // Contador de likes (desnormalizado para rendimiento)
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    @JsonIgnoreProperties({"restaurant", "hibernateLazyInitializer", "handler"})
    private Menu menu;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User user;
    
    @OneToMany(mappedBy = "rating", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RatingImage> images = new ArrayList<>();

    // Relación con likes - NO se expone en JSON (solo el contador)
    @OneToMany(mappedBy = "rating", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RatingLike> likes = new ArrayList<>();

    // Constructor para datos seed (sin likes)
    public Rating(Long id, Integer score, String comment, Menu menu, User user, List<RatingImage> images) {
        this.id = id;
        this.score = score;
        this.comment = comment;
        this.likesCount = 0;
        this.menu = menu;
        this.user = user;
        this.images = images != null ? images : new ArrayList<>();
        this.likes = new ArrayList<>();
    }

    // Métodos helper para manejar likes
    public void incrementLikes() {
        this.likesCount = (this.likesCount == null ? 0 : this.likesCount) + 1;
    }

    public void decrementLikes() {
        this.likesCount = (this.likesCount == null || this.likesCount <= 0) ? 0 : this.likesCount - 1;
    }
}
