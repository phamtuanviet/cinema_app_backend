package com.example.my_movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCinemaDto {
    private UUID id;
    private String name;
    private String address;
    private String description;
    private String region;
    private String cineplex;
    private Double latitude;
    private Double longitude;
    private String logoUrl;
    private Boolean isActive;
}