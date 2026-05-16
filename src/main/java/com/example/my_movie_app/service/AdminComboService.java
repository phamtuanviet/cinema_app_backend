package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.AdminComboCreateRequest;
import com.example.my_movie_app.dto.AdminComboDto;
import com.example.my_movie_app.dto.AdminComboUpdateRequest;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.entity.Combo;
import com.example.my_movie_app.repository.ComboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminComboService {

    private final ComboRepository comboRepository;
    private final CloudinaryService cloudinaryService;

    public AdminPaginatedResponse<AdminComboDto> getCombos(String search, Boolean isActive, int page, int size) {

        // 1. Chuẩn hóa chuỗi tìm kiếm (Chuyển về chữ thường và bọc %)
        String searchParam = null;
        if (search != null && !search.trim().isEmpty()) {
            searchParam = "%" + search.trim().toLowerCase() + "%";
        }

        // 2. Phân trang & Sắp xếp theo tên A-Z
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        // 3. Truy vấn Database
        Page<Combo> comboPage = comboRepository.searchCombosByStatus(searchParam, isActive, pageable);

        // 4. Ánh xạ từ Entity sang DTO
        List<AdminComboDto> dtoList = comboPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // 5. Đóng gói trả về
        return new AdminPaginatedResponse<>(
                dtoList,
                comboPage.getNumber(),
                comboPage.getTotalPages(),
                comboPage.getTotalElements(),
                comboPage.isLast()
        );
    }

    // Hàm Map dùng chung cho cả tính năng Create/Edit sau này


    public AdminComboDto getComboById(UUID id) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Combo bắp nước này"));
        return mapToDto(combo);
    }

    // 2. API Cập nhật Combo
    @Transactional
    public AdminComboDto createCombo(AdminComboCreateRequest request, MultipartFile image) {
        Combo combo = new Combo();

        // 1. Cập nhật thông tin cơ bản
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setPrice(request.getPrice());

        if (request.getIsActive() != null) {
            combo.setIsActive(request.getIsActive());
        } else {
            combo.setIsActive(true); // Mặc định là true khi tạo mới
        }

        // 2. Xử lý ảnh bằng Cloudinary
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            combo.setImageUrl(imageUrl);
        }

        // 3. Lưu và trả về
        Combo savedCombo = comboRepository.save(combo);
        return mapToDto(savedCombo);
    }

    // 🔥 HÀM SỬA: CẬP NHẬT COMBO (Đã update theo mẫu của bạn)
    @Transactional
    public AdminComboDto updateCombo(UUID id, AdminComboUpdateRequest request, MultipartFile image) {
        // 1. Tìm Combo theo ID
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Combo bắp nước với ID: " + id));

        // 2. Cập nhật Ảnh NẾU client có gửi file mới lên
        if (image != null && !image.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(image);
            combo.setImageUrl(newImageUrl);
        }
        // Nếu image == null, giữ nguyên url ảnh cũ trong DB

        // 3. Cập nhật các thông tin cơ bản
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setPrice(request.getPrice());

        if (request.getIsActive() != null) {
            combo.setIsActive(request.getIsActive());
        }

        // 4. Lưu và trả về
        Combo savedCombo = comboRepository.save(combo);
        return mapToDto(savedCombo);
    }

    // Hàm mapToDto
    public AdminComboDto mapToDto(Combo combo) {
        return AdminComboDto.builder()
                .id(combo.getId())
                .name(combo.getName())
                .description(combo.getDescription())
                .price(combo.getPrice())
                .imageUrl(combo.getImageUrl())
                .isActive(combo.getIsActive())
                .build();
    }
}