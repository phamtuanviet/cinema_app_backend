package com.example.my_movie_app.dto.response;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class HoldSeatResponse {

    private String id;
    private String seatRow;
    private Integer seatNumber;
    private String seatHoldSessionId;
    private Boolean isSuccess;
    private String expiresAt;
    private Double price;
}