package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private BigDecimal amount;

    private String paymentMethod;


    private String gatewayTransactionId;

    // mã order gửi sang gateway (vnp_TxnRef)
    private String gatewayOrderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status ;

    private String gatewayResponseCode;

    private LocalDateTime paymentTime = LocalDateTime.now();
}