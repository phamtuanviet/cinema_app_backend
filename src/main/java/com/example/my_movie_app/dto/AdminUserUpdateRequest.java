package com.example.my_movie_app.dto;


import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String role;
    private Boolean isBanned;
    private Boolean isVerified;
}