package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.SeatHoldSession;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findBySession(SeatHoldSession session);

    // 🔥 Lấy booking theo session
    Optional<Booking> findBySession_Id(UUID sessionId);

    // 🔥 (OPTIONAL - để optimize tránh N+1)
    @Query("""
        SELECT b FROM Booking b
        WHERE b.session.id IN :sessionIds
    """)
    List<Booking> findBySessionIdIn(@Param("sessionIds") Set<UUID> sessionIds);

    Booking findByTicketCode(String ticketCode);


    @Query("""
    SELECT DISTINCT b FROM Booking b
    LEFT JOIN FETCH b.showtime s
    LEFT JOIN FETCH s.movie
    LEFT JOIN FETCH s.room r
    LEFT JOIN FETCH r.cinema
    LEFT JOIN FETCH b.bookingCombos bc
    LEFT JOIN FETCH bc.combo
    WHERE b.user.id = :userId
    AND b.status = 'PAID'
""")
    List<Booking> findAllByUserId(UUID userId);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "JOIN FETCH b.showtime s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.room r " +
            "JOIN FETCH r.cinema c " +
            "LEFT JOIN FETCH b.session sess " + // LEFT JOIN vì có thể booking cũ không còn session
            "LEFT JOIN FETCH sess.seatReservations sr " +
            "LEFT JOIN FETCH sr.seat " +
            "WHERE b.user.id = :userId " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") UUID userId, Pageable pageable);
}