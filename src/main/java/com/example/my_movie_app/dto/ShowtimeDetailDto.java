package com.example.my_movie_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeDetailDto {
    private String id;
    private String cinemaName;
    private String date; // dd-MM-yyyy
    private String time; // HH:mm
    private Integer durationMinutes;
    private String room;
}
