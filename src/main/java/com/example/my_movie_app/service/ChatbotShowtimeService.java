package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.ShowtimeChatbotResponse;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.ShowtimeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    @Transactional(readOnly = true)
    public List<ShowtimeChatbotResponse> getShowtimes(String movieTitle, String dateString, String region) {

        // 1. CHUẨN HÓA MÚI GIỜ (Tránh lỗi server quốc tế lấy nhầm giờ UTC)
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime now = LocalDateTime.now(zoneId);

        LocalDateTime fromDate;
        LocalDateTime toDate; // Lúc nào cũng sẽ được gán giá trị, không bao giờ null

        // 2. CHUẨN HÓA THAM SỐ CHUỖI (Ngăn lỗi NullPointerException)
        String title = (movieTitle == null) ? "" : movieTitle.trim();
        String loc = (region == null) ? "" : region.trim();

        // 3. LOGIC THỜI GIAN (Xử lý tuyệt đối không lọt suất chiếu quá khứ)
        if (dateString != null && !dateString.isBlank()) {
            if (dateString.contains("T")) {
                // TRƯỜNG HỢP A: Hỏi đích danh Ngày + Giờ (VD: 2026-04-05T20:00:00)
                LocalDateTime targetDateTime = LocalDateTime.parse(dateString);

                if (targetDateTime.isBefore(now)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dạ, em không thể tìm lịch chiếu trong quá khứ được ạ.");
                }

                // Tìm từ giờ khách chọn đến cuối ngày hôm đó
                fromDate = targetDateTime;
                toDate = targetDateTime.toLocalDate().atTime(LocalTime.MAX);

            } else {
                // TRƯỜNG HỢP B: Chỉ hỏi Ngày (VD: 2026-04-05)
                LocalDate targetDate = LocalDate.parse(dateString);

                if (targetDate.isEqual(now.toLocalDate())) {
                    // Nếu là "hôm nay": Bắt đầu tìm từ "ngay bây giờ" (Tránh lọt suất buổi sáng)
                    fromDate = now;
                } else if (targetDate.isBefore(now.toLocalDate())) {
                    // Nếu hỏi ngày hôm qua
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dạ, em không thể tìm lịch chiếu trong quá khứ được ạ.");
                } else {
                    // Nếu là ngày mai, ngày kia: Bắt đầu tìm từ 00h00 sáng ngày đó
                    fromDate = targetDate.atStartOfDay();
                }
                // Kết thúc vào cuối ngày (23:59:59)
                toDate = targetDate.atTime(LocalTime.MAX);
            }
        } else {
            // TRƯỜNG HỢP C: Không có thời gian -> Tìm từ bây giờ đến hết 3 ngày tới
            fromDate = now;
            toDate = now.toLocalDate().plusDays(3).atTime(LocalTime.MAX);
        }

        // 4. TRUY VẤN DATABASE
        List<Showtime> showtimes = showtimeRepository.findShowtimesForChatbot(title, loc, fromDate, toDate);

        // 5. TRẢ KẾT QUẢ HOẶC BÁO LỖI
        if (showtimes.isEmpty()) {
            String message = "Dạ, em không tìm thấy lịch chiếu cho phim '" + (movieTitle != null ? movieTitle : "") + "'";
            if (region != null && !region.isBlank()) message += " tại khu vực " + region;

            // Nếu tìm theo giờ cụ thể mà không có, gợi ý thêm
            if (dateString != null && dateString.contains("T")) {
                message += " vào khung giờ này. Anh/chị có thể chọn một khung giờ khác được không ạ?";
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }

        return showtimes.stream().map(this::mapToShowtimeResponse).collect(Collectors.toList());
    }

    private ShowtimeChatbotResponse mapToShowtimeResponse(Showtime showtime) {
        // Cộng giá cơ bản của phim (hoặc rạp) với phụ phí cuối tuần (nếu có)
        // Giả sử basePrice lấy từ Showtime
        java.math.BigDecimal finalPrice = showtime.getBasePrice();
        if (showtime.getWeekendModifier() != null) {
            finalPrice = finalPrice.add(showtime.getWeekendModifier());
        }

        return ShowtimeChatbotResponse.builder()
                .movieTitle(showtime.getMovie().getTitle())
                .cinemaName(showtime.getRoom().getCinema().getName())
                .cinemaAddress(showtime.getRoom().getCinema().getAddress())
                .roomName(showtime.getRoom().getName())
                .startTime(showtime.getStartTime())
                .price(finalPrice)
                .build();
    }
}