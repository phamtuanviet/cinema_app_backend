package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.RevenueChartPoint;
import com.example.my_movie_app.dto.RevenueSummaryDto;
import com.example.my_movie_app.entity.Payment;
import com.example.my_movie_app.enums.PaymentStatus;
import com.example.my_movie_app.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminRevenueService {

    private final PaymentRepository paymentRepository;

    public RevenueSummaryDto getRevenueSummary(String timeRange, String targetDateStr) {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        // Nếu Frontend truyền ngày cụ thể thì dùng nó, không thì lấy hôm nay
        LocalDate baseDate = (targetDateStr != null && !targetDateStr.isEmpty())
                ? LocalDate.parse(targetDateStr)
                : LocalDate.now(zone);

        // Ép baseDate thành LocalDateTime để dùng các hàm cũ
        LocalDateTime now = baseDate.atTime(LocalTime.MAX);

        LocalDateTime startDate;
        LocalDateTime endDate;
        List<RevenueChartPoint> chartData = new ArrayList<>();

        // 1. XÁC ĐỊNH KHOẢNG THỜI GIAN VÀ TẠO TRỤC X CHO BIỂU ĐỒ (Kể cả khi doanh thu = 0)
        switch (timeRange.toUpperCase()) {
            case "DAY": // Hôm nay (Chia làm 6 mốc, mỗi mốc 4 tiếng)
                startDate = now.toLocalDate().atStartOfDay();
                endDate = startDate.plusDays(1).minusNanos(1);
                for (int i = 0; i < 24; i += 4) {
                    chartData.add(new RevenueChartPoint(String.format("%02d:00", i), 0.0));
                }
                break;
            case "WEEK": // Tuần này (T2 -> CN)
                startDate = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
                endDate = startDate.plusDays(7).minusNanos(1);
                String[] daysOfWeek = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                for (String day : daysOfWeek) {
                    chartData.add(new RevenueChartPoint(day, 0.0));
                }
                break;
            case "MONTH": // Tháng này (Các ngày trong tháng)
                startDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                endDate = now.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(LocalTime.MAX);
                int daysInMonth = YearMonth.from(now).lengthOfMonth();
                // Nếu vẽ 30 cột thì quá sát nhau, ta vẽ mốc cách nhau 5 ngày
                for (int i = 1; i <= daysInMonth; i += 5) {
                    chartData.add(new RevenueChartPoint("N" + i, 0.0));
                }
                break;
            case "YEAR": // Năm nay (Tháng 1 -> 12)
                startDate = now.withDayOfYear(1).toLocalDate().atStartOfDay();
                endDate = now.with(TemporalAdjusters.lastDayOfYear()).toLocalDate().atTime(LocalTime.MAX);
                for (int i = 1; i <= 12; i++) {
                    chartData.add(new RevenueChartPoint("Th" + i, 0.0));
                }
                break;
            default:
                throw new IllegalArgumentException("Time range không hợp lệ: " + timeRange);
        }

        // 2. QUERY DATABASE
        List<Payment> payments = paymentRepository.findByStatusAndPaymentTimeBetween(
                PaymentStatus.SUCCESS, startDate, endDate
        );

        // 3. TÍNH TỔNG QUAN
        double totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        int totalTransactions = payments.size();

        // 4. ĐỔ DỮ LIỆU VÀO BIỂU ĐỒ
        for (Payment p : payments) {
            LocalDateTime time = p.getPaymentTime();
            double amount = p.getAmount() != null ? p.getAmount().doubleValue() : 0.0;
            String labelToMatch = "";

            switch (timeRange.toUpperCase()) {
                case "DAY":
                    int hourBucket = (time.getHour() / 4) * 4; // Ép về các mốc 0, 4, 8, 12, 16, 20
                    labelToMatch = String.format("%02d:00", hourBucket);
                    break;
                case "WEEK":
                    int dayIndex = time.getDayOfWeek().getValue() - 1; // 0=T2, 6=CN
                    String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                    labelToMatch = days[dayIndex];
                    break;
                case "MONTH":
                    int day = time.getDayOfMonth();
                    int dayBucket = ((day - 1) / 5) * 5 + 1; // Ép về 1, 6, 11, 16...
                    labelToMatch = "N" + dayBucket;
                    break;
                case "YEAR":
                    labelToMatch = "Th" + time.getMonthValue();
                    break;
            }

            // Tìm cột trên biểu đồ và cộng dồn tiền vào
            for (RevenueChartPoint point : chartData) {
                if (point.getLabel().equals(labelToMatch)) {
                    point.setValue(point.getValue() + amount);
                    break;
                }
            }
        }

        return RevenueSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .totalSuccessfulTransactions(totalTransactions)
                .chartData(chartData)
                .build();
    }
}