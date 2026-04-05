package com.example.my_movie_app.dto.request;

import lombok.Data;

@Data
public class MovieSearchRequest {
    private String keyword;
    private String genre;
    private String language;
    private String ageRating;
    private Integer page = 0;
    private Integer size = 10;
}
