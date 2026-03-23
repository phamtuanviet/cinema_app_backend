package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.SeatHoldSession;
import io.lettuce.core.dynamic.annotation.Param;
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
}