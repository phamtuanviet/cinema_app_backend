package com.example.my_movie_app.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatRowDto {
    private String row;
    private List<SeatDto> seats;
}