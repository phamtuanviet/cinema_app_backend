package com.example.my_movie_app.repository;


import com.example.my_movie_app.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComboRepository extends JpaRepository<Combo, UUID> {

    List<Combo> findByIsActiveTrue();

}
