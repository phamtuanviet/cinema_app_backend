package com.example.my_movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMovieDto {
    private UUID id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private BigDecimal basePrice;
    private String posterUrl;
    private String trailerUrl;
    private String ageRating;
    private String language;
    private Boolean isActive;
    private List<AdminGenreDto> genres;
}