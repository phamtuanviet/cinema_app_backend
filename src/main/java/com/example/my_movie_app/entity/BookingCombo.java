package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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