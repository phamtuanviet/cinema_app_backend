package com.example.my_movie_app.dto.mapper;

import com.example.my_movie_app.dto.ShowtimeDetailDto;
import com.example.my_movie_app.entity.Showtime;

import java.time.format.DateTimeFormatter;

public class ShowtimeMapper {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    public static ShowtimeDetailDto toDto(Showtime showtime) {
        if (showtime == null) return null;

        return ShowtimeDetailDto.builder()
                .id(showtime.getId().toString())
                .cinemaName(showtime.getRoom().getCinema().getName())
                .date(showtime.getStartTime().format(DATE_FORMAT))
                .time(showtime.getStartTime().format(TIME_FORMAT))
                .durationMinutes(
                        (int) java.time.Duration.between(
                                showtime.getStartTime(),
                                showtime.getEndTime()
                        ).toMinutes()
                )
                .room(showtime.getRoom().getName())
                .build();
    }
}