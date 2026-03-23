package com.example.my_movie_app.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaShowtimeDto {

    private String cinemaId;
    private String cinemaName;
    private Double distanceKm;
    private List<ShowtimeDto> showtimes;
}