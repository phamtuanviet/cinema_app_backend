package com.example.my_movie_app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminShowtimeUpdateRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // Chỉ nhận ACTIVE hoặc CANCELED
}