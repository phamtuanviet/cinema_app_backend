package com.example.my_movie_app.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
}