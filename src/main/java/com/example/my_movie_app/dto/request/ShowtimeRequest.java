package com.example.my_movie_app.dto.request;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ShowtimeRequest {

    private UUID movieId;
    private UUID roomId;
    private LocalDateTime startTime;

    private BigDecimal basePrice; // giá gốc (VND)
}