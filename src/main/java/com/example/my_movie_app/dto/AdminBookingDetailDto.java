package com.example.my_movie_app.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingDetailDto {
    private UUID id;
    private String ticketCode;
    private String qrCodeUrl;
    private String status;
    private String createdAt;
    private String cancelledAt;

    // Khách hàng
    private String userName;
    private String userEmail;
    private String userPhone;

    // Phim & Suất chiếu
    private String movieName;
    private String moviePosterUrl;
    private String cinemaName;
    private String roomName;
    private String showtimeTime;

    // Dịch vụ
    private String seats;
    private String combos;

    // Thanh toán
    private BigDecimal seatAmount;
    private BigDecimal comboAmount;
    private BigDecimal voucherDiscount;
    private BigDecimal pointDiscount;
    private BigDecimal totalAmount;
}