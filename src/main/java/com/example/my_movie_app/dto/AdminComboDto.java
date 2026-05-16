package com.example.my_movie_app.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminComboDto {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean isActive;
}