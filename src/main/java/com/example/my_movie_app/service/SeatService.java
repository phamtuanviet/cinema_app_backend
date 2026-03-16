package com.example.my_movie_app.service;

import com.example.my_movie_app.entity.Seat;
import com.example.my_movie_app.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    public List<Seat> getSeatsByRoom(UUID roomId) {
        return seatRepository.findByRoomId(roomId);
    }

}