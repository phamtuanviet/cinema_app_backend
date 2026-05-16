package com.example.my_movie_app.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BannerRequest {
    private String imageUrl;
    private String actionType; // Sẽ nhận chuỗi "URL" hoặc "MOVIE" từ file JSON
    private String targetUrl;  // Thay thế cho actionValue
    private UUID movieId;      // Thêm trường ID phim
    private Boolean isActive;
    private Integer priority;
}