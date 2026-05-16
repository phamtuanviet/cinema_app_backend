package com.example.my_movie_app.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminVoucherUpdateRequest {
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private String expiryDate; // Client gửi định dạng ISO: "2026-12-31T23:59:00"
    private Boolean active;
    private Integer usageLimit;
}