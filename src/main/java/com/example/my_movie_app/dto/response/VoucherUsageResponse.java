package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class VoucherUsageResponse {

    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private LocalDateTime showtime;

    private BigDecimal discountAmount;

    private LocalDateTime usedAt;
}