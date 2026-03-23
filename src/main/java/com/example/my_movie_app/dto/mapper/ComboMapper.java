package com.example.my_movie_app.dto.mapper;

import com.example.my_movie_app.dto.ComboDto;
import com.example.my_movie_app.entity.Combo;

public class ComboMapper {

    public static ComboDto toDto(Combo combo) {
        if (combo == null) return null;

        return ComboDto.builder()
                .id(combo.getId().toString())
                .name(combo.getName())
                .description(combo.getDescription())
                .price(combo.getPrice())
                .imageUrl(combo.getImageUrl())
                .isAvailable(combo.getIsActive())
                .build();
    }
}