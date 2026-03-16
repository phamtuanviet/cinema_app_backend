package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CinemaRepository extends JpaRepository<Cinema, UUID> {
    List<Cinema> findByIsActiveTrue();

    List<Cinema> findByRegion(String region);
}