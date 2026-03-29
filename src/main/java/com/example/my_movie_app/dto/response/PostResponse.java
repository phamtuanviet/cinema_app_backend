package com.example.my_movie_app.dto.response;

import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.enums.PostType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private UUID id;
    private String title;
    private String thumbnailUrl;
    private PostType type;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}