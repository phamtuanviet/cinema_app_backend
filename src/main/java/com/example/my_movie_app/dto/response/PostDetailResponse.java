package com.example.my_movie_app.dto.response;


import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.VoucherPostDto;
import com.example.my_movie_app.enums.PostType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {

    private UUID id;

    private String title;
    private String content;
    private String thumbnailUrl;

    private Boolean published;
    private PostType type;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // ===== Voucher info (nếu có) =====
    private VoucherPostDto voucher;

    // ===== computed field cho UI =====
    private Boolean isActive; // post còn hiệu lực không
}