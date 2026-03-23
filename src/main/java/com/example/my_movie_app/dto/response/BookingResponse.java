package com.example.my_movie_app.dto.response;

import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.ShowtimeDetailDto;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

    private String bookingId;
    private String ticketCode;
    private String qrCodeUrl;
    private BigDecimal seatAmount;
    private BigDecimal comboAmount;
    private BigDecimal voucherDiscount;
    private BigDecimal pointDiscount;
    private BigDecimal totalAmount;

    private String status; // PENDING / PAID

    private String createdAt;
}