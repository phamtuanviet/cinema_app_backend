package com.example.my_movie_app.dto;

import lombok.Data;

@Data
public class AdminCinemaUpdateRequest {
    private String name;
    private String address;
    private String description;
    private String region;
    private String cineplex;
    private Double latitude;  // Sẽ được Android tự động bóc tách và gửi lên
    private Double longitude;
    private Boolean isActive;
}