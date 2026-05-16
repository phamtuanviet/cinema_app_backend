package com.example.my_movie_app.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AdminUserVoucherDto {
    private UUID id;
    private String voucherCode;
    private String discountType;
    private Double discountValue;
    private Boolean isUsed;
    private String usedAt;
}