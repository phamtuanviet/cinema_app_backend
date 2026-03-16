package com.example.my_movie_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
public class SeatHoldSession {

    @Id
    private UUID id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Showtime showtime;

    private Instant expiresAt;

    private Instant createdAt;

    @OneToMany(mappedBy = "session")
    private List<SeatReservation> seatReservations;

}