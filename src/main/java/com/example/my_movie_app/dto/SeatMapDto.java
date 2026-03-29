package com.example.my_movie_app.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatMapDto {
    private String showtimeId;
    private List<SeatRowDto> rows;
    private String expiresAt;
    private String seatHoldSessionId;
}