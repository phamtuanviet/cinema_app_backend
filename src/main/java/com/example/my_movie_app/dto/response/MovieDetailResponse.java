package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MovieDetailResponse {
    private UUID movieId;
    private String title;
    private String description;
    private String posterUrl;
    private String trailerUrl;
    private List<String> genres;
    private Integer durationMinutes;
    private String ageRating;
    private String language;
    private LocalDate releaseDate;
    private Double averageRating;
    private Integer ratingCount; // Thêm trường đếm số lượt đánh giá
}