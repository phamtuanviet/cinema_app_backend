package com.example.my_movie_app.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldSeatRequest {
    private UUID showtimeId;
    private UUID seatId;

}
