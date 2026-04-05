package com.example.my_movie_app.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class NowPlayingRequest {
    private String locationQuery;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    private Integer page = 0;
    private Integer size = 10;
}