package com.example.my_movie_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenuePointDto {
    private String date;
    private Double amount;
}