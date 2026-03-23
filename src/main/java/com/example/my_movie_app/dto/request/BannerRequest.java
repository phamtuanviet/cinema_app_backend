package com.example.my_movie_app.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BannerRequest {
    private String imageUrl;
    private String actionType;
    private String actionValue;
    private Boolean isActive;
    private Integer priority;
}