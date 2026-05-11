package com.example.my_movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPaginatedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int totalPages;
    private long totalElements;
    private boolean isLast;
}
