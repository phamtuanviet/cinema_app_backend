package com.example.my_movie_app.dto.response;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoyaltyTransactionResponse {

    private Integer points;
    private String type;
    private String description;

    private String movieTitle;
    private String cinemaName;
    private LocalDateTime showtime;

    private LocalDateTime createdAt;
}