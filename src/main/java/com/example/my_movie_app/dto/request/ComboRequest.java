package com.example.my_movie_app.dto.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class ComboRequest {
    private String name;
    private String description;
    private Double price;

}