package com.example.my_movie_app.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private UUID id;
    private String email;
    private String role;
}