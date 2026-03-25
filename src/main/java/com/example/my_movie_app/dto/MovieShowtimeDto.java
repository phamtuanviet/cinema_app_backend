package com.example.my_movie_app.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieShowtimeDto {

    private UUID movieId;
    private String title;
    private Integer duration;
    private String ageRating;
    private String posterUrl;
    private String trailerUrl;
    private List<String> genres;
    private List<ShowtimeDto> showtimes;
}