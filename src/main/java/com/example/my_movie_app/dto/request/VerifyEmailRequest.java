package com.example.my_movie_app.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class VerifyEmailRequest {
    private String email;
    private String otp;
}
