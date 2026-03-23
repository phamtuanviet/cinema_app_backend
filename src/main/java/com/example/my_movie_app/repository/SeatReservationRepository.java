package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Seat;
import com.example.my_movie_app.entity.SeatReservation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SeatReservationRepository extends JpaRepository<SeatReservation, UUID> {
    @Query("""
    SELECT sr FROM SeatReservation sr
    JOIN FETCH sr.seat
    LEFT JOIN FETCH sr.session s
    LEFT JOIN FETCH Booking b ON b.session = s
    WHERE sr.showtime.id = :showtimeId
      AND sr.isCancel = false
""")
    List<SeatReservation> findAllByShowtimeId(UUID showtimeId);

    @Query("""
        SELECT sr FROM SeatReservation sr
        WHERE sr.seat.id = :seatId
        AND sr.showtime.id = :showtimeId
        AND sr.isCancel = false
    """)
    Optional<SeatReservation> findActiveBySeatAndShowtime(
            @Param("seatId") UUID seatId,
            @Param("showtimeId") UUID showtimeId
    );

    // 🔥 Lấy reservation của chính user (để cancel)
    @Query("""
        SELECT sr FROM SeatReservation sr
        WHERE sr.seat.id = :seatId
        AND sr.showtime.id = :showtimeId
        AND sr.session.user.id = :userId
        AND sr.isCancel = false
    """)
    Optional<SeatReservation> findBySeatAndShowtimeAndUser(
            @Param("seatId") UUID seatId,
            @Param("showtimeId") UUID showtimeId,
            @Param("userId") UUID userId
    );

    // 🔥 Lấy tất cả reservation của showtime (dùng render seat map)
    @Query("""
        SELECT sr FROM SeatReservation sr
        JOIN FETCH sr.session s
        WHERE sr.showtime.id = :showtimeId
        AND sr.isCancel = false
    """)
    List<SeatReservation> findAllActiveByShowtime(
            @Param("showtimeId") UUID showtimeId
    );

    List<SeatReservation> findBySession_IdAndIsCancelFalse(UUID sessionId);

}