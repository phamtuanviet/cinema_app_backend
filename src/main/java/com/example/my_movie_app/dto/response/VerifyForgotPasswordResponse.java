package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class VerifyForgotPasswordResponse {
    private String resetToken;
}
