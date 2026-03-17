package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.request.ShowtimeRequest;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/showtime")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PostMapping
    public Showtime create(@RequestBody ShowtimeRequest request) {
        return showtimeService.createShowtime(request);
    }
}