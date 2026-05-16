package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.AdminBannerCreateRequest;
import com.example.my_movie_app.dto.AdminBannerDto;
import com.example.my_movie_app.dto.AdminBannerUpdateRequest;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.entity.Banner;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.enums.BannerActionType;
import com.example.my_movie_app.repository.BannerRepository;
import com.example.my_movie_app.repository.MovieRepository;
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
public class AdminBannerService {

    private final BannerRepository bannerRepository;
    private final MovieRepository movieRepository; // Nhớ inject cái này vào

    public AdminPaginatedResponse<AdminBannerDto> getBanners(String search, String actionTypeStr, int page, int size) {

        // 1. Chuẩn hóa chuỗi tìm kiếm tránh lỗi bytea
        String searchParam = "";
        if (search != null && !search.trim().isEmpty()) {
            searchParam = "%" + search.trim() + "%"; // Không cần toUpper vì Query JPQL đã gọi UPPER()
        }

        // 2. Ép kiểu Enum an toàn
        BannerActionType type;
        try {
            type = BannerActionType.valueOf(actionTypeStr.toUpperCase());
        } catch (Exception e) {
            type = BannerActionType.MOVIE; // Mặc định Tab Phim
        }

        // 3. Phân trang & Sắp xếp: Ưu tiên (Priority giảm dần) -> Thời gian tạo (Mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priority")
                .and(Sort.by(Sort.Direction.DESC, "createdAt")));

        // 4. Gọi DB Query
        Page<Banner> bannerPage = bannerRepository.searchBanners(searchParam, type, pageable);

        // 5. Ánh xạ ra DTO
        List<AdminBannerDto> dtoList = bannerPage.getContent().stream()
                .map(this::mapToAdminDto)
                .collect(Collectors.toList());

        return new AdminPaginatedResponse<>(
                dtoList,
                bannerPage.getNumber(),
                bannerPage.getTotalPages(),
                bannerPage.getTotalElements(),
                bannerPage.isLast()
        );
    }

    // Hàm Map Entity sang DTO và lookup tên phim
    public AdminBannerDto mapToAdminDto(Banner banner) {
        String movieName = null;

        // Nếu là MOVIE thì lookup sang bảng Movie để lấy Tên phim
        if (banner.getActionType() == BannerActionType.MOVIE && banner.getMovieId() != null) {
            movieName = movieRepository.findById(banner.getMovieId())
                    .map(Movie::getTitle)
                    .orElse("Phim không tồn tại hoặc đã bị xóa");
        }

        return AdminBannerDto.builder()
                .id(banner.getId())
                .imageUrl(banner.getImageUrl())
                .actionType(banner.getActionType() != null ? banner.getActionType().name() : "URL")
                .targetUrl(banner.getTargetUrl())
                .movieId(banner.getMovieId())
                .movieName(movieName)
                .priority(banner.getPriority())
                .isActive(banner.getIsActive())
                .build();
    }

    private final CloudinaryService cloudinaryService;

    // Lấy chi tiết để hiển thị lên Form
    public AdminBannerDto getBannerById(UUID id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại"));
        return mapToAdminDto(banner);
    }

    // Cập nhật Banner có Upload Ảnh
    @Transactional
    public AdminBannerDto updateBanner(UUID id, AdminBannerUpdateRequest request, MultipartFile image) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Banner"));

        // Cập nhật các trường thông thường
        try {
            banner.setActionType(BannerActionType.valueOf(request.getActionType().toUpperCase()));
        } catch (Exception e) {
            banner.setActionType(BannerActionType.URL);
        }

        banner.setTargetUrl(request.getTargetUrl());
        banner.setMovieId(request.getMovieId());
        banner.setPriority(request.getPriority());
        if (request.getIsActive() != null) banner.setIsActive(request.getIsActive());

        // Logic làm sạch rác (Bảo vệ tính nhất quán của DB)
        if (banner.getActionType() == BannerActionType.URL) {
            banner.setMovieId(null);
        } else if (banner.getActionType() == BannerActionType.MOVIE) {
            banner.setTargetUrl(null);
        }

        // Upload ảnh mới nếu có
        if (image != null && !image.isEmpty()) {
            banner.setImageUrl(cloudinaryService.uploadImage(image));
        }

        Banner saved = bannerRepository.save(banner);
        return mapToAdminDto(saved);
    }

    @Transactional
    public AdminBannerDto createBanner(AdminBannerCreateRequest request, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Banner bắt buộc phải có hình ảnh đính kèm");
        }

        Banner banner = new Banner();

        try {
            banner.setActionType(BannerActionType.valueOf(request.getActionType().toUpperCase()));
        } catch (Exception e) {
            banner.setActionType(BannerActionType.URL);
        }

        banner.setTargetUrl(request.getTargetUrl());
        banner.setMovieId(request.getMovieId());
        banner.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        banner.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Làm sạch rác trước khi lưu
        if (banner.getActionType() == BannerActionType.URL) {
            banner.setMovieId(null);
        } else if (banner.getActionType() == BannerActionType.MOVIE) {
            banner.setTargetUrl(null);
        }

        // Upload ảnh lên Cloudinary
        try {
            String imageUrl = cloudinaryService.uploadImage(image);
            banner.setImageUrl(imageUrl);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload ảnh Banner: " + e.getMessage());
        }

        Banner savedBanner = bannerRepository.save(banner);
        return mapToAdminDto(savedBanner);
    }
}