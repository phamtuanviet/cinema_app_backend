package com.example.my_movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AdminMovieSimpleDto {
    private UUID id;
    private String title;
}