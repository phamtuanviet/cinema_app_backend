package com.example.my_movie_app.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingComboMyBookingDto {
    private String comboName;
    private Integer quantity;
    private BigDecimal price;
}