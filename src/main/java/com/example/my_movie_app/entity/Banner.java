package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.BannerActionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner extends BaseEntity {

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private BannerActionType actionType;

    // Dùng khi actionType = URL
    private String targetUrl;

    // Dùng khi actionType = MOVIE
    private UUID movieId;

    private Integer priority = 0;

    private Boolean isActive = true;
}