package com.example.my_movie_app.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AdminBannerCreateRequest {
    private String actionType;
    private String targetUrl;
    private UUID movieId;
    private Integer priority;
    private Boolean isActive;
}