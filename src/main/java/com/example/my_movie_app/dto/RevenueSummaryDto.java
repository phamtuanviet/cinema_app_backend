package com.example.my_movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryDto {
    private Double totalRevenue;
    private Integer totalSuccessfulTransactions;
    private List<RevenueChartPoint> chartData;
}