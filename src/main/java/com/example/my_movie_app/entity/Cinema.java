package com.example.my_movie_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
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

    @Column(columnDefinition = "TEXT")
    private String description;

    private String region;
    private String cineplex;
    private Double latitude;
    private Double longitude;
    private Boolean isActive = true;
}