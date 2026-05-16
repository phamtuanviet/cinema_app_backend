package com.example.my_movie_app.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AdminLoyaltyTransactionDto {
    private UUID id;
    private Integer points;
    private String type;
    private String description;
    private String createdAt;
}