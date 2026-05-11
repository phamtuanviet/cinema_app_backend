package com.example.my_movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// DTO dùng chung cho Dropdown (Phim, Rạp, Phòng)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleItemDto {
    private UUID id;
    private String name;
}