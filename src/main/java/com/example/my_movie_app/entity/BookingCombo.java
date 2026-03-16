package com.example.my_movie_app.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

import java.math.BigDecimal;

@Entity
public class BookingCombo {

    @EmbeddedId
    private BookingComboId id;

    @ManyToOne
    @MapsId("bookingId")
    private Booking booking;

    @ManyToOne
    @MapsId("comboId")
    private Combo combo;

    private Integer quantity;

    private BigDecimal price;

}