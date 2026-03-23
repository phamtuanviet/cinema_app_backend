package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.CinemaShowtimeDto;
import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.request.ShowtimeRequest;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/showtime")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PostMapping
    public Showtime create(@RequestBody ShowtimeRequest request) {
        return showtimeService.createShowtime(request);
    }



    @GetMapping("/cinema-showtimes")
    public ResponseEntity<List<CinemaShowtimeDto>> getCinemaShowtimes(
            @RequestParam UUID movieId,
            @RequestParam String date,
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return ResponseEntity.ok(
                showtimeService.getCinemaShowtimes(
                        movieId,
                        LocalDate.parse(date),
                        lat,
                        lng
                )
        );
    }

    @GetMapping("/{movieId}/show-dates")
    public ResponseEntity<List<LocalDate>> getShowDates(
            @PathVariable UUID movieId
    ) {
        return ResponseEntity.ok(showtimeService.getNext10ShowDates(movieId));
    }

    @GetMapping("/{showtimeId}/movie")
    public ResponseEntity<MovieDto> getMovieByShowtime(
            @PathVariable UUID showtimeId
    ) {
        return ResponseEntity.ok(
                showtimeService.getMovieByShowtime(showtimeId)
        );
    }


}