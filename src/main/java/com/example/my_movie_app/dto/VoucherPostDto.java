package com.example.my_movie_app.dto;

import com.example.my_movie_app.enums.DiscountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherPostDto {

    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;

    private LocalDateTime expiryDate;

    private Boolean active;

    private Integer usageLimit;
    private Integer usedCount;

    // tiện cho UI
    private Integer remainingUsage;
}