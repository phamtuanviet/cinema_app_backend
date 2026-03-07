package com.example.my_movie_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "cinemas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cinema extends BaseEntity {

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String hotline;
    private Boolean isActive = true;
}