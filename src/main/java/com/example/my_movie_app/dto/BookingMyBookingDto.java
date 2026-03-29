package com.example.my_movie_app.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingMyBookingDto {

    private UUID id;
    private String ticketCode;
    private String qrCodeUrl;

    private String status;

    private BigDecimal seatAmount;
    private BigDecimal comboAmount;
    private BigDecimal voucherDiscount;
    private BigDecimal pointDiscount;
    private BigDecimal totalAmount;

    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;

    private MovieMyBookingDto movie;
    private CinemaMyBookingDto cinema;
    private RoomMyBookingDto room;

    private List<SeatMyBookingDto> seats;
    private List<BookingComboMyBookingDto> combos;

    private Integer userRating;
    private Double averageRating;
}

