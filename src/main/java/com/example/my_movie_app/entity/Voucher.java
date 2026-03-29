package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.DiscountType;
import com.example.my_movie_app.enums.VoucherDiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Voucher extends BaseEntity {

    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal minOrderValue;

    private BigDecimal maxDiscount;

    private LocalDateTime expiryDate;

    private Boolean active = true;

    private Integer usageLimit;

    @Column(nullable = false)
    private Integer usedCount = 0;


}