package com.example.my_movie_app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminVoucherCreateRequest {
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private String expiryDate; // Nhận chuỗi ISO từ Kotlin
    private Boolean active;
    private Integer usageLimit;
}