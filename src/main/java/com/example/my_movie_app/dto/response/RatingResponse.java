package com.example.my_movie_app.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private UUID movieId;
    private Integer userScore;
    private Double averageScore;
}