package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByCinemaId(UUID cinemaId);

}