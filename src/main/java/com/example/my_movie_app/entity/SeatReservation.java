package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatReservation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne
    private SeatHoldSession session;

    @Column(name = "is_cancel", nullable = false)
    private boolean isCancel = false;
}