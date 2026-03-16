package com.example.my_movie_app.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class BookingComboId implements Serializable {

    private UUID bookingId;

    private Long comboId;

}