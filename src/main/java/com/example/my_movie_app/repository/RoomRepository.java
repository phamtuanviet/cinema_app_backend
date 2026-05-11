package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Room;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByCinemaId(UUID cinemaId);

    @Query("SELECT r FROM Room r WHERE r.cinema.id = :cinemaId AND r.id NOT IN " +
            "(SELECT s.room.id FROM Showtime s WHERE s.room.cinema.id = :cinemaId " +
            "AND s.startTime < :endTime AND s.endTime > :startTime)")
    List<Room> findAvailableRooms(
            @Param("cinemaId") UUID cinemaId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

}