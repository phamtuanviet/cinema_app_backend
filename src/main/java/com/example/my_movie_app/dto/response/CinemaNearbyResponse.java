package com.example.my_movie_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor // Cần constructor này để Mapping từ Native Query
public class CinemaNearbyResponse {
    private UUID id;
    private String name;
    private String address;
    private String cineplex;
    private Double distance; // Khoảng cách tính bằng km
    private String logoUrl;
}