package com.example.my_movie_app.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerDto{
    private UUID id;
    private String imageUrl;
    private String actionType;
    private String actionValue;
}
