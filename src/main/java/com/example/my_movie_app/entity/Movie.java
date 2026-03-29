package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer durationMinutes;

    private LocalDate releaseDate;

    private BigDecimal basePrice;

    private String posterUrl;
    private String trailerUrl;
    private String ageRating;
    private String language;

    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;
}