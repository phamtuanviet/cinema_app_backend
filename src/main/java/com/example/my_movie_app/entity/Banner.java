package com.example.my_movie_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner extends BaseEntity {
    private String imageUrl;
    private String actionType;
    private String actionValue;
    private Integer priority = 0;
    private Boolean isActive;
}