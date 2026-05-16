package com.example.my_movie_app.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AdminPostUpdateRequest {
    private String title;
    private String content;
    private Boolean published;
    private String type; // "NORMAL" hoặc "VOUCHER"
    private String startDate; // Nhận ISO: yyyy-MM-dd'T'HH:mm:ss
    private String endDate;
    private UUID voucherId; // ID của Voucher được chọn (nếu có)
}