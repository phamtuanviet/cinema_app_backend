package com.example.my_movie_app.controller;


import com.example.my_movie_app.entity.Seat;
import com.example.my_movie_app.service.SeatService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/room/{roomId}")
    public List<Seat> getSeats(@PathVariable UUID roomId) {
        return seatService.getSeatsByRoom(roomId);
    }

}