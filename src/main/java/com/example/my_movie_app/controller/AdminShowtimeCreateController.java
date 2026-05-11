package com.example.my_movie_app.controller;


import com.example.my_movie_app.dto.AdminShowtimeCreateRequest;
import com.example.my_movie_app.dto.AdminShowtimeDto;
import com.example.my_movie_app.dto.SimpleItemDto;
import com.example.my_movie_app.service.AdminShowtimeCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminShowtimeCreateController {

    private final AdminShowtimeCreateService createService;

    // API: /api/admin/movies/search
    @GetMapping("/movies/search")
    public List<SimpleItemDto> searchMovies(@RequestParam String query) {
        return createService.searchMovies(query);
    }

    // API: /api/admin/cinemas/search
    @GetMapping("/cinemas/search")
    public List<SimpleItemDto> searchCinemas(@RequestParam String query) {
        return createService.searchCinemas(query);
    }

    // API: /api/admin/rooms/available
    @GetMapping("/rooms/available")
    public List<SimpleItemDto> getAvailableRooms(
            @RequestParam UUID cinemaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return createService.getAvailableRooms(cinemaId, startTime, endTime);
    }

    // API: /api/admin/showtimes (POST)
    @PostMapping("/showtimes")
    public AdminShowtimeDto createShowtime(@RequestBody AdminShowtimeCreateRequest request) {
        return createService.createShowtime(request);
    }
}