package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Room;
import com.example.my_movie_app.entity.Showtime;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {
    boolean existsByRoomAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Room room,
            String status,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    List<Showtime> findByStatusAndStartTimeBetween(String status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT DATE(s.startTime) FROM Showtime s " +
            "WHERE s.movie.id = :movieId " +
            "AND s.startTime BETWEEN :start AND :end " +
            "ORDER BY DATE(s.startTime)")
    List<LocalDate> findAvailableDates(
            UUID movieId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT DISTINCT m
    FROM Showtime s
    JOIN s.movie m
    LEFT JOIN FETCH m.genres
    WHERE s.startTime > :now
    AND m.releaseDate <= :today
""")
    List<Movie> findNowShowing(
            @Param("now") LocalDateTime now,
            @Param("today") LocalDate today
    );

    @Query("""
    SELECT DISTINCT m
    FROM Showtime s
    JOIN s.movie m
    LEFT JOIN FETCH m.genres
    WHERE m.releaseDate > :today
""")
    List<Movie> findComingSoon(@Param("today") LocalDate today);

    @Query(value = """
    SELECT DISTINCT DATE(s.start_time)
    FROM showtimes  s
    WHERE s.movie_id = :movieId
    AND s.start_time > :now
    ORDER BY DATE(s.start_time)
    LIMIT 10
""", nativeQuery = true)
    List<java.sql.Date> findNext10ShowDates(UUID movieId, LocalDateTime now);


    @Query("""
    SELECT s
    FROM Showtime s
    JOIN FETCH s.room r
    JOIN FETCH r.cinema c
    WHERE s.movie.id = :movieId
    AND s.startTime BETWEEN :start AND :end
    AND s.status = 'ACTIVE'
""")
    List<Showtime> findShowtimesByMovieAndTime(
            @Param("movieId") UUID movieId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    boolean existsByMovieIdAndStartTimeAfter(UUID movieId, LocalDateTime time);

    @Query("""
    SELECT DISTINCT s FROM Showtime s
    JOIN FETCH s.movie m
    LEFT JOIN FETCH m.genres
    WHERE s.id = :id
""")
    Optional<Showtime> findFullById(@Param("id") UUID id);

    @Query("""
    SELECT DISTINCT CAST(s.startTime AS date)
    FROM Showtime s
    JOIN s.room r
    JOIN r.cinema c
    WHERE c.id = :cinemaId
      AND s.status = 'ACTIVE'
      AND s.startTime > CURRENT_TIMESTAMP
    ORDER BY CAST(s.startTime AS date)
""")
    List<java.sql.Date> findDistinctShowDates(UUID cinemaId);

    @Query("""
        SELECT s FROM Showtime s
        JOIN FETCH s.movie m
        JOIN FETCH s.room r
        JOIN FETCH r.cinema c
        LEFT JOIN FETCH m.genres g
        WHERE c.id = :cinemaId
        AND DATE(s.startTime) = :date
        AND s.status = 'ACTIVE'
        ORDER BY m.id, s.startTime
    """)
    List<Showtime> findByCinemaAndDate(UUID cinemaId, LocalDate date);
}