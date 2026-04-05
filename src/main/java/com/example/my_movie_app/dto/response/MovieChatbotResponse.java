package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MovieChatbotResponse {
    private UUID movieId;
    private String title;
    private String posterUrl;
    private List<String> genres;
    private Integer durationMinutes;
    private String ageRating;
    private Double averageRating; // Bổ sung từ entity Rating
}