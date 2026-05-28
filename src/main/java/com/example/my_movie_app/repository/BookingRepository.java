package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.SeatHoldSession;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.projection.RevenueProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findFirstBySession(SeatHoldSession session, Sort sort);

    @Query("SELECT b FROM Booking b JOIN b.session s WHERE b.status = :status AND s.expiresAt < :now")
    List<Booking> findByStatusAndSession_ExpiresAtBefore(BookingStatus status, Instant now);

    // 🔥 Lấy booking theo session
    Optional<Booking> findBySession_Id(UUID sessionId);

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
            "LEFT JOIN FETCH b.session sess " +
            "LEFT JOIN FETCH sess.seatReservations sr " +
            "LEFT JOIN FETCH sr.seat " +
            "WHERE b.user.id = :userId " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
    SELECT DISTINCT b FROM Booking b
    LEFT JOIN FETCH b.showtime s
    LEFT JOIN FETCH s.movie
    LEFT JOIN FETCH s.room r
    LEFT JOIN FETCH r.cinema
    LEFT JOIN FETCH b.bookingCombos bc
    LEFT JOIN FETCH bc.combo
    WHERE b.id = :id 
    AND b.user.id = :userId
""")
    Optional<Booking> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.session WHERE b.id = :id")
    Optional<Booking> findByIdWithSession(@Param("id") UUID id);

    @Query(value = """
        SELECT to_char(b.created_at, 'DD/MM') as date, SUM(b.total_amount) as amount 
        FROM bookings b 
        WHERE b.created_at >= CURRENT_DATE - INTERVAL '7 days' 
          AND b.status = 'PAID' 
        GROUP BY to_char(b.created_at, 'DD/MM'), DATE(b.created_at) 
        ORDER BY DATE(b.created_at) ASC
    """, nativeQuery = true)
    List<RevenueProjection> getRevenueLast7Days();

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND " +
            "(:search IS NULL OR " +
            "LOWER(b.user.email) LIKE :search OR " +
            "LOWER(b.ticketCode) LIKE :search)")
    Page<Booking> searchBookingsByStatus(
            @Param("search") String search,
            @Param("status") BookingStatus status,
            Pageable pageable
    );
}