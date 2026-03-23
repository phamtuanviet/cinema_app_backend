package com.example.my_movie_app.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ResetPasswordRequest {
    private String resetPassword;
    private String password;
}
