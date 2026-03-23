package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"genres"})
    List<Movie> findAllByIdIn(List<UUID> ids);

    List<Movie> findByTitleContainingIgnoreCase(String keyword);

    boolean existsByTitleAndReleaseDate(String title, LocalDate releaseDate);

    @EntityGraph(attributePaths = {"genres"})
    Optional<Movie> findById(UUID id);

    List<Movie> findByIsActiveTrueAndReleaseDateBefore(LocalDate date); // now showing
    List<Movie> findByIsActiveTrueAndReleaseDateAfter(LocalDate date);
}