package com.example.my_movie_app.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class AdminMovieCreateRequest {
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private BigDecimal basePrice;
    private String ageRating;
    private String language;
    private String trailerUrl;
    private Boolean isActive;
    private Boolean sendNotification;
    private List<UUID> genreIds;     // Các ID thể loại đã có
    private List<String> newGenres;  // Các tên thể loại mới cần tạo
}