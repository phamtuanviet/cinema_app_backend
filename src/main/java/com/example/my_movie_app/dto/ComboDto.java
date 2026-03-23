package com.example.my_movie_app.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboDto {

    private String id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean isAvailable;
}