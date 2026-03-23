package com.example.my_movie_app.dto.request;


import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MovieRequest {

    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String ageRating;
    private String language;
    private Boolean isActive;

    private List<GenreRequest> genres;

    @Data
    public static class GenreRequest {
        private String name;
    }
}