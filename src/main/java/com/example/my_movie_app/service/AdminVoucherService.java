package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.*;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.enums.DiscountType;
import com.example.my_movie_app.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVoucherService {

    private final VoucherRepository voucherRepository;

    public AdminPaginatedResponse<AdminVoucherDto> getVouchers(String search, String status, int page, int size) {

        // 1. Chuẩn hóa chuỗi tìm kiếm (Chữ in hoa và bọc % cho mã CODE)
        String searchParam = ""; // 🔥 SỬA Ở ĐÂY: Dùng chuỗi rỗng thay vì null
        if (search != null && !search.trim().isEmpty()) {
            // Đã toUpperCase() ở Java rồi nên DB không cần làm nữa
            searchParam = "%" + search.trim().toUpperCase() + "%";
        }

        // 2. Validate status (Bảo vệ API, nếu gửi linh tinh thì ép về VALID)
        String targetStatus = "INVALID".equalsIgnoreCase(status) ? "INVALID" : "VALID";

        // 3. Phân trang & Sắp xếp (Voucher mới tạo lên đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 4. Truy vấn DB, truyền LocalDateTime.now() xuống
        Page<Voucher> voucherPage = voucherRepository.searchVouchersByStatus(
                searchParam,
                targetStatus,
                LocalDateTime.now(),
                pageable
        );

        // 5. Ánh xạ Entity -> DTO
        List<AdminVoucherDto> dtoList = voucherPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new AdminPaginatedResponse<>(
                dtoList,
                voucherPage.getNumber(),
                voucherPage.getTotalPages(),
                voucherPage.getTotalElements(),
                voucherPage.isLast()
        );
    }

    // Hàm Map dùng chung cho sau này
    public AdminVoucherDto mapToDto(Voucher voucher) {
        String expiryDateStr = null;
        if (voucher.getExpiryDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
            expiryDateStr = voucher.getExpiryDate().format(formatter);
        }

        return AdminVoucherDto.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType() != null ? voucher.getDiscountType().name() : "PERCENT")
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscount(voucher.getMaxDiscount())
                .expiryDate(expiryDateStr)
                .active(voucher.getActive())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .build();
    }

    public AdminVoucherDto getVoucherById(UUID id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Voucher với ID: " + id));
        return mapToDto(voucher);
    }

    // 2. Cập nhật Voucher
    @Transactional
    public AdminVoucherDto updateVoucher(UUID id, AdminVoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Voucher"));

        // Cập nhật mã code (Đảm bảo in hoa)
        if (request.getCode() != null) {
            voucher.setCode(request.getCode().trim().toUpperCase());
        }

        // Cập nhật Loại giảm giá (PERCENT hoặc FIXED)
        try {
            if (request.getDiscountType() != null) {
                voucher.setDiscountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            voucher.setDiscountType(DiscountType.PERCENT); // Mặc định nếu lỗi
        }

        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());

        // Logic bảo vệ: Nếu là FIXED (Tiền mặt) thì không có maxDiscount
        if (voucher.getDiscountType() == DiscountType.FIXED) {
            voucher.setMaxDiscount(null);
        } else {
            voucher.setMaxDiscount(request.getMaxDiscount());
        }

        voucher.setUsageLimit(request.getUsageLimit());

        // Nếu client không gửi active thì giữ nguyên, có thì cập nhật
        if (request.getActive() != null) {
            voucher.setActive(request.getActive());
        }

        // Xử lý Parse ngày tháng (expiryDate)
        if (request.getExpiryDate() != null && !request.getExpiryDate().isEmpty()) {
            try {
                // Parse chuỗi "2026-12-31T23:59:00" thành LocalDateTime
                LocalDateTime parsedDate = LocalDateTime.parse(request.getExpiryDate());
                voucher.setExpiryDate(parsedDate);
            } catch (Exception e) {
                throw new RuntimeException("Định dạng ngày hết hạn không hợp lệ");
            }
        } else {
            // Nếu client gửi lên null hoặc chuỗi rỗng -> Xóa hạn sử dụng (Không giới hạn thời gian)
            voucher.setExpiryDate(null);
        }

        // Lưu xuống DB (usedCount không cho phép sửa tay nên ta không đụng vào)
        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToDto(savedVoucher);
    }

    @Transactional
    public AdminVoucherDto createVoucher(AdminVoucherCreateRequest request) {
        String formattedCode = request.getCode().trim().toUpperCase();

        // 1. Kiểm tra trùng mã Code (Bảo vệ tính Unique)
        // Giả định bạn có hàm findByCode trong UserRepository / VoucherRepository
        if (voucherRepository.existsByCode(formattedCode)) {
            throw new RuntimeException("Mã voucher '" + formattedCode + "' đã tồn tại trên hệ thống!");
        }

        // 2. Khởi tạo Entity mới
        Voucher voucher = new Voucher();
        voucher.setCode(formattedCode);
        voucher.setUsedCount(0); // Luôn khởi tạo bằng 0 lượt dùng

        // Parse loại giảm giá
        try {
            voucher.setDiscountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            voucher.setDiscountType(DiscountType.PERCENT);
        }

        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());

        // Nếu là giảm tiền mặt FIXED thì ép maxDiscount = null
        if (voucher.getDiscountType() == DiscountType.FIXED) {
            voucher.setMaxDiscount(null);
        } else {
            voucher.setMaxDiscount(request.getMaxDiscount());
        }

        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setActive(request.getActive() != null ? request.getActive() : true);

        // Parse thời gian hết hạn
        if (request.getExpiryDate() != null && !request.getExpiryDate().isEmpty()) {
            try {
                voucher.setExpiryDate(LocalDateTime.parse(request.getExpiryDate()));
            } catch (Exception e) {
                throw new RuntimeException("Định dạng ngày hết hạn không chính xác");
            }
        } else {
            voucher.setExpiryDate(null);
        }

        // 3. Lưu và Map sang DTO trả về
        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToDto(savedVoucher);
    }

    public List<AdminVoucherSimpleDto> getActiveVouchers() {
        // Tìm các voucher đang bật (active = true) và chưa hết hạn
        return voucherRepository.findAll().stream()
                .filter(Voucher::getActive)
                .filter(v -> v.getExpiryDate() == null || v.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(v -> new AdminVoucherSimpleDto(v.getId(), v.getCode()))
                .collect(Collectors.toList());
    }
}