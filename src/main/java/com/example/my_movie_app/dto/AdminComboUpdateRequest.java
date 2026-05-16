package com.example.my_movie_app.dto;

import lombok.Data;

@Data
public class AdminComboUpdateRequest {
    private String name;
    private String description;
    private Double price;
    private Boolean isActive;
}