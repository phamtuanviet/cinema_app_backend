package com.example.my_movie_app.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private Boolean isVerified;
    private String avatarUrl;
}
