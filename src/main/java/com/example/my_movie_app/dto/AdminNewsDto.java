package com.example.my_movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminNewsDto {
    private UUID id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private Boolean published;
    private String type;
    private String startDate;
    private String endDate;
    private String voucherCode;
}