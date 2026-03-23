package com.example.my_movie_app.entity;


import com.example.my_movie_app.enums.UsageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUsage {

    @Id
    private UUID id;

    @ManyToOne
    private Voucher voucher;

    @ManyToOne
    private User user;

    @ManyToOne
    private Booking booking;

    @OneToOne
    private UserVoucher userVoucher;

    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    private UsageStatus status;

    private LocalDateTime usedAt;
}
