package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {
    @EmbeddedId
    private BookingSeatId id;

    @ManyToOne
    @MapsId("bookingId")
    private Booking booking;

    @ManyToOne
    @MapsId("seatId")
    private Seat seat;

    private BigDecimal seatPrice;

    @ManyToOne
    private Showtime showtime;
}