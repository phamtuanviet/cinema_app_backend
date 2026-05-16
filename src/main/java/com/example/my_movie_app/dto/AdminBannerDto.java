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
public class AdminBannerDto {
    private UUID id;
    private String imageUrl;
    private String actionType;
    private String targetUrl;
    private UUID movieId;
    private String movieName; // Tên phim hiển thị
    private Integer priority;
    private Boolean isActive;
}
