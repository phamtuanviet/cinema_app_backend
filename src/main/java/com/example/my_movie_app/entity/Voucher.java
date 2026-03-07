package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.VoucherDiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private VoucherDiscountType discountType = VoucherDiscountType.FIXED;

    private BigDecimal discountAmount;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minSpend = BigDecimal.ZERO;

    private Integer usageLimit;

    private Boolean isActive = true;

    private LocalDateTime expiresAt;
}