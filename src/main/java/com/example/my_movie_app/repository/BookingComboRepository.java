package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.BookingCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingComboRepository extends JpaRepository<BookingCombo, UUID> {

}