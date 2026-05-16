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
public class AdminUserDto {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private Boolean isVerified;
    private String role;
    private String avatarUrl;
    private Boolean isBanned;
}