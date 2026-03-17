package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Room;
import com.example.my_movie_app.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {
    boolean existsByRoomAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Room room,
            String status,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}