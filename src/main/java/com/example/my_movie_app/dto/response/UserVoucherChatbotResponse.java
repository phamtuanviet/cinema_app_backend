package com.example.my_movie_app.dto.response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserVoucherChatbotResponse {
    private String voucherCode;
    private String displayName;
    private String description;
    private LocalDateTime expiryDate;
    private String status;
    private BigDecimal minOrderValue;
}