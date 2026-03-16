package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "combos")
public class Combo {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String description;

    private Double price;

    private String imageUrl;

    private Boolean isActive = true;

}