package com.example.my_movie_app.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CancelSeatCoupleResponse {
    private String seatHoldSessionId;
    private String expiresAt;
    private Double price;
}
