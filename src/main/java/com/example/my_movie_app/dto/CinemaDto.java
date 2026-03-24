package com.example.my_movie_app.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaDto {
    private UUID id;
    private String name;
    private String address;
    private Double distance;
    private Double latitude;
    private Double longitude;
    private String logoUrl;
}
