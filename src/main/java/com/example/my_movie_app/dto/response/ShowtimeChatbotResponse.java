package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShowtimeChatbotResponse {
    private String movieTitle;
    private String cinemaName;
    private String cinemaAddress;
    private String roomName;
    private LocalDateTime startTime;
    private BigDecimal price;
}