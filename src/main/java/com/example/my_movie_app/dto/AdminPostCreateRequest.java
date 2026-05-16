package com.example.my_movie_app.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AdminPostCreateRequest {
    private String title;
    private String content;
    private Boolean published;
    private String type;
    private String startDate;
    private String endDate;
    private UUID voucherId;
}