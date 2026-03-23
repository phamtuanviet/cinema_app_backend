package com.example.my_movie_app.dto.response;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ComboResponse {

    private UUID id;

    private String name;

    private String description;

    private Double price;

    private String imageUrl;

}