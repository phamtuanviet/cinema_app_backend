package com.example.my_movie_app.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionDto {
    private String region;
    private Long totalCinema;
}
