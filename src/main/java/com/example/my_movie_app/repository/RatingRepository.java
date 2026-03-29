package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    @Query("""
    SELECT r.movie.id, AVG(r.score)
    FROM Rating r
    WHERE r.movie.id IN :movieIds
    GROUP BY r.movie.id
""")
    List<Object[]> getAverageRatingsByMovieIds(Set<UUID> movieIds);

    @Query("""
    SELECT r FROM Rating r
    WHERE r.user.id = :userId
    AND r.movie.id IN :movieIds
""")
    List<Rating> findByUserIdAndMovieIds(UUID userId,Set<UUID> movieIds);

    Optional<Rating> findByUserIdAndMovieId(UUID userId, UUID movieId);

    List<Rating> findByMovieId(UUID movieId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.movie.id = :movieId")
    Double getAverageScore(UUID movieId);
}
