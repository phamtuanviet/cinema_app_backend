package com.example.my_movie_app.dto.request;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequest {

    private String seatHoldSessionId;

    // comboId -> quantity
    private Map<String, Integer> selectedCombos;

    private String voucherId;

    private Integer usedPoints;
}