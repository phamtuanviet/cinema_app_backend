package com.example.my_movie_app.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieMyBookingDto {
    private UUID id;
    private String title;
    private String posterUrl;
}