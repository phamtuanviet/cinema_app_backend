package com.example.my_movie_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatMyBookingDto {
    private String seatRow;
    private Integer seatNumber;
}
