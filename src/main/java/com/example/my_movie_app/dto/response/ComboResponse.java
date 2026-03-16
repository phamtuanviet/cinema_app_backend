package com.example.my_movie_app.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ComboResponse {

    private UUID id;

    private String name;

    private String description;

    private Double price;

    private String imageUrl;

}