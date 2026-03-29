package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserVoucherResponse {


    private UUID id;
    private String code;

    private String discountType;
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;

    private LocalDateTime expiryDate;

    private Boolean isUsed;

    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private LocalDateTime showtime;

    private BigDecimal discountAmount;
    private LocalDateTime usedAt;
}