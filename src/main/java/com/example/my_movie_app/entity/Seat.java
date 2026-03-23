package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {

    @ManyToOne
    private Room room;

    private String seatRow;

    private Integer seatNumber;


    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType = SeatType.NORMAL;

    private BigDecimal priceModifier;

}