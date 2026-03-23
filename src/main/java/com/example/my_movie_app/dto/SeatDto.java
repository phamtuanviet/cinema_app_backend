package com.example.my_movie_app.dto;


import com.example.my_movie_app.enums.SeatStatus;
import com.example.my_movie_app.enums.SeatType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDto {
    private String id;
    private String seatRow;
    private Integer seatNumber;
    private SeatType seatType;
    private Double price;
    private SeatStatus status;
}