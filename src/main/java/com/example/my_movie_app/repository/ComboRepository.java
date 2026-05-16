package com.example.my_movie_app.repository;


import com.example.my_movie_app.entity.Combo;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComboRepository extends JpaRepository<Combo, UUID> {

    List<Combo> findByIsActiveTrue();

    @Query("SELECT c FROM Combo c WHERE c.isActive = :isActive AND " +
            "(:search IS NULL OR LOWER(c.name) LIKE :search)")
    Page<Combo> searchCombosByStatus(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
