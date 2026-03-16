package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @Column(unique = true, nullable = false)
    private String ticketCode;

    private String qrCodeUrl;

    private BigDecimal seatAmount;

    private BigDecimal comboAmount;

    private BigDecimal voucherDiscount;

    private BigDecimal pointDiscount;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime cancelledAt;

    @ManyToOne
    private SeatHoldSession session;

    @OneToMany(mappedBy = "booking")
    private List<Payment> payments;
}