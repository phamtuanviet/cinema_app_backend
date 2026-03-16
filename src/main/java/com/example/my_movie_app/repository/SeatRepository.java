package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByRoomId(UUID roomId);

}