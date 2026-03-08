package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CinemaRepository extends JpaRepository<Cinema, UUID> {
}