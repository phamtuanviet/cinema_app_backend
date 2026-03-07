package com.example.my_movie_app.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
}