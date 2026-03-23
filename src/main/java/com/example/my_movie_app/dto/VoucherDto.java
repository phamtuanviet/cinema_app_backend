package com.example.my_movie_app.dto;

import com.example.my_movie_app.enums.DiscountType;
import com.example.my_movie_app.enums.VoucherDiscountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDto {
    private String id;
    private String code;
    private String title;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;
    private LocalDateTime expiryDate;
    private Boolean isUsable;
}