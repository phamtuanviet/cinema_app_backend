package com.example.my_movie_app.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class AdminMovieUpdateRequest {
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private BigDecimal basePrice;
    private String ageRating;
    private String language;
    private String trailerUrl;
    private Boolean isActive;

    private List<UUID> genreIds;
    private List<String> newGenres;
}