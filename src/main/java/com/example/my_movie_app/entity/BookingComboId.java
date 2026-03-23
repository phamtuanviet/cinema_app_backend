package com.example.my_movie_app.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class BookingComboId implements Serializable {

    private UUID bookingId;

    private UUID comboId;

}