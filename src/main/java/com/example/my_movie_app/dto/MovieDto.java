package com.example.my_movie_app.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDto {
    private UUID id;
    private String title;
    private Integer durationMinutes;
    private String posterUrl;
    private String ageRating;
    private String language;
    private String trailerUrl;
    private String releaseDate;
    private String description;
    private List<String> genres;
}
