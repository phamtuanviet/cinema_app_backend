package com.example.my_movie_app.dto.response;
import com.example.my_movie_app.dto.CinemaDto;
import com.example.my_movie_app.dto.MovieShowtimeDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaShowtimeResponse {

    private CinemaDto cinema;
    private LocalDate date;
    private List<MovieShowtimeDto> movies;
}