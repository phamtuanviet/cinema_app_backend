package com.example.my_movie_app.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPointsResponse {
    private Integer availablePoints;
    private String monetaryValueFormatted; // VD: "10,000 VNĐ"
    private String accumulationPolicy;     // VD: "Tích lũy 2% mỗi lần đặt vé"
    private String note;
}