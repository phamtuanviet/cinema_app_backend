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
public class AdminBookingDto {
    private UUID id;
    private String ticketCode;
    private String userEmail;
    private String movieName;
    private String showtimeTime;
    private BigDecimal totalAmount;
    private String status;
}