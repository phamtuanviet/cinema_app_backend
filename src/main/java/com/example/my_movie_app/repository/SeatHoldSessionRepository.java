package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Seat;
import com.example.my_movie_app.entity.SeatHoldSession;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



public interface SeatHoldSessionRepository extends JpaRepository<SeatHoldSession, UUID> {

    @Query("""
        SELECT s FROM SeatHoldSession s
        WHERE s.user.id = :userId
        AND s.showtime.id = :showtimeId
        AND s.expiresAt > :now
    """)
    Optional<SeatHoldSession> findActiveSession(
            @Param("userId") UUID userId,
            @Param("showtimeId") UUID showtimeId,
            @Param("now") Instant now
    );

    @Query("""
    SELECT DISTINCT s FROM SeatHoldSession s
    JOIN FETCH s.user
    JOIN FETCH s.showtime st
    JOIN FETCH st.movie m
    JOIN FETCH st.room r
    JOIN FETCH r.cinema
    LEFT JOIN FETCH s.seatReservations sr
    LEFT JOIN FETCH sr.seat
    WHERE s.id = :id
""")
    Optional<SeatHoldSession> findByIdWithFullData(UUID id);

    Optional<SeatHoldSession> findByIdAndUser_Id(UUID sessionId, UUID userId);



    @Modifying
    @Query(value = """
    UPDATE seat_hold_session
    SET expires_at = expires_at + (:minutes * INTERVAL '1 minute')
    WHERE id = :id
      AND expires_at > NOW()
""", nativeQuery = true)
    int extendSession(UUID id, int minutes);
}

