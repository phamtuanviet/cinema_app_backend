package com.example.my_movie_app.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class HoldSeatCoupleRequest {
    private UUID showtimeId;
    private UUID firstSeatId;
    private UUID secondSeatId;
}
