package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder

public class LogoutResponse {
    private String message;
}
