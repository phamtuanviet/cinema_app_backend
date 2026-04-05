package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingHistoryResponse {
    private String ticketCode;
    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private LocalDateTime startTime;
    private String seats; // Ví dụ: "A1, A2"
    private BigDecimal totalAmount;
    private String statusDisplay; // "Thành công", "Đã hủy", "Hết hạn", "Chờ thanh toán"
    private String qrCodeUrl;
}