package com.example.my_movie_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalUsers;
    private long totalMovies;
    private long totalCinemas;
    private long totalShowtimes;
}