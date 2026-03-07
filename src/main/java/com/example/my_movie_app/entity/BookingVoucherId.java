package com.example.my_movie_app.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingVoucherId implements Serializable {

    private UUID bookingId;
    private UUID voucherId;
}