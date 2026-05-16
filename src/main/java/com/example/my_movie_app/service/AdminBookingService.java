package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.AdminBookingDetailDto;
import com.example.my_movie_app.dto.AdminBookingDto;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final BookingRepository bookingRepository;

    public AdminPaginatedResponse<AdminBookingDto> getBookings(String search, String statusStr, int page, int size) {

        // 1. Chuẩn hóa chuỗi tìm kiếm trên Java
        String searchParam = null;
        if (search != null && !search.trim().isEmpty()) {
            searchParam = "%" + search.trim().toLowerCase() + "%";
        }

        // 2. Chuyển String Status thành Enum BookingStatus an toàn
        BookingStatus targetStatus;
        try {
            targetStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            targetStatus = BookingStatus.PAID; // Mặc định hiển thị vé đã thanh toán
        }

        // 3. Phân trang & Sắp xếp (Vé mới nhất hiện trên cùng - dùng created_at)
        // Nếu BaseEntity của bạn có trường createdAt, thay "id" thành "createdAt"
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 4. Gọi DB
        Page<Booking> bookingPage = bookingRepository.searchBookingsByStatus(searchParam, targetStatus, pageable);

        // 5. Ánh xạ Entity -> DTO
        List<AdminBookingDto> dtoList = bookingPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // 6. Đóng gói Response
        return new AdminPaginatedResponse<>(
                dtoList,
                bookingPage.getNumber(),
                bookingPage.getTotalPages(),
                bookingPage.getTotalElements(),
                bookingPage.isLast()
        );
    }

    private AdminBookingDto mapToDto(Booking booking) {
        String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : "Khách vãng lai";
        String movieName = "Chưa cập nhật";
        String showtimeTime = "Chưa cập nhật";

        // Trích xuất an toàn Tên Phim và Giờ Chiếu
        if (booking.getShowtime() != null) {
            if (booking.getShowtime().getMovie() != null) {
                movieName = booking.getShowtime().getMovie().getTitle();
            }
            LocalDateTime startTime = booking.getShowtime().getStartTime();
            if (startTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
                showtimeTime = startTime.format(formatter);
            }
        }

        return AdminBookingDto.builder()
                .id(booking.getId())
                .ticketCode(booking.getTicketCode())
                .userEmail(userEmail)
                .movieName(movieName)
                .showtimeTime(showtimeTime)
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus() != null ? booking.getStatus().name() : "PENDING")
                .build();
    }

    @Transactional(readOnly = true) // Bắt buộc phải có để Lazy Loading các list Combos, Session...
    public AdminBookingDetailDto getBookingDetail(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đơn đặt vé với ID: " + id));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

        // 1. Trích xuất thông tin Khách hàng
        String userName = booking.getUser() != null && booking.getUser().getFullName() != null ? booking.getUser().getFullName() : "Khách vãng lai";
        String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : "N/A";
        String userPhone = booking.getUser() != null ? booking.getUser().getPhone() : null;

        // 2. Trích xuất thông tin Suất chiếu
        String movieName = "N/A";
        String posterUrl = null;
        String cinemaName = "N/A";
        String roomName = "N/A";
        String showtimeTime = "N/A";

        if (booking.getShowtime() != null) {
            if (booking.getShowtime().getMovie() != null) {
                movieName = booking.getShowtime().getMovie().getTitle();
                posterUrl = booking.getShowtime().getMovie().getPosterUrl();
            }
            if (booking.getShowtime().getRoom() != null) {
                roomName = booking.getShowtime().getRoom().getName();
                if (booking.getShowtime().getRoom().getCinema() != null) {
                    cinemaName = booking.getShowtime().getRoom().getCinema().getName();
                }
            }
            if (booking.getShowtime().getStartTime() != null) {
                showtimeTime = booking.getShowtime().getStartTime().format(formatter);
            }
        }

        // 3. Xử lý danh sách Ghế (Trích xuất từ SeatHoldSession)
        // GIẢ ĐỊNH: session.getSelectedSeats() trả về list chứa đối tượng Seat có seatRow và seatNumber
        String seatsStr = "";
        if (booking.getSession() != null && booking.getSession().getSeatReservations() != null) {
            seatsStr = booking.getSession().getSeatReservations().stream()
                    // Lọc bỏ những ghế đã bị hủy (isCancel = true)
                    .filter(reservation -> !reservation.isCancel())
                    // Lấy ra thông tin Ghế từ Reservation
                    .map(reservation -> {
                        if (reservation.getSeat() != null) {
                            return reservation.getSeat().getSeatRow() + reservation.getSeat().getSeatNumber();
                        }
                        return "";
                    })
                    .filter(seat -> !seat.isEmpty()) // Bỏ qua nếu lỗi null
                    .collect(Collectors.joining(", "));
        }

        // 4. Xử lý danh sách Combo
        // GIẢ ĐỊNH: BookingCombo có getQuantity() và getCombo().getName()
        String combosStr = "";
        if (booking.getBookingCombos() != null && !booking.getBookingCombos().isEmpty()) {
            combosStr = booking.getBookingCombos().stream()
                    .map(bc -> bc.getQuantity() + "x " + (bc.getCombo() != null ? bc.getCombo().getName() : "Combo"))
                    .collect(Collectors.joining(", "));
        }

        // 5. Build DTO trả về
        return AdminBookingDetailDto.builder()
                .id(booking.getId())
                .ticketCode(booking.getTicketCode())
                .qrCodeUrl(booking.getQrCodeUrl())
                .status(booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN")

                // Bạn thay "getCreatedAt" bằng hàm lấy ngày tạo trong BaseEntity của bạn
                .createdAt(booking.getCreatedAt() != null ? booking.getCreatedAt().format(formatter) : "N/A")
                .cancelledAt(booking.getCancelledAt() != null ? booking.getCancelledAt().format(formatter) : null)

                .userName(userName)
                .userEmail(userEmail)
                .userPhone(userPhone)

                .movieName(movieName)
                .moviePosterUrl(posterUrl)
                .cinemaName(cinemaName)
                .roomName(roomName)
                .showtimeTime(showtimeTime)

                .seats(seatsStr)
                .combos(combosStr)

                // Mấy field này có sẵn trong entity Booking của bạn rồi
                .seatAmount(booking.getSeatAmount() != null ? booking.getSeatAmount() : BigDecimal.ZERO)
                .comboAmount(booking.getComboAmount() != null ? booking.getComboAmount() : BigDecimal.ZERO)
                .voucherDiscount(booking.getVoucherDiscount() != null ? booking.getVoucherDiscount() : BigDecimal.ZERO)
                .pointDiscount(booking.getPointDiscount() != null ? booking.getPointDiscount() : BigDecimal.ZERO)
                .totalAmount(booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO)
                .build();
    }
}