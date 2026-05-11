package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.AdminCinemaCreateRequest;
import com.example.my_movie_app.dto.AdminCinemaDto;
import com.example.my_movie_app.dto.AdminCinemaUpdateRequest;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.dto.request.CreateRoomRequest;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.repository.CinemaRepository;
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
public class AdminCinemaService {

    private final CinemaRepository cinemaRepository;
    private final CloudinaryService cloudinaryService;
    private final RoomService roomService;

    public AdminPaginatedResponse<AdminCinemaDto> getCinemas(String search, int page, int size) {

        // Sắp xếp theo tên rạp (Hoặc bạn có thể đổi thành Sort.by(Sort.Direction.DESC, "createdAt") nếu BaseEntity có trường này)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        Page<Cinema> cinemaPage;

        if (search != null && !search.trim().isEmpty()) {
            cinemaPage = cinemaRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            cinemaPage = cinemaRepository.findAll(pageable);
        }

        // Convert List<Entity> sang List<DTO>
        List<AdminCinemaDto> dtoList = cinemaPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // Đóng gói vào chuẩn PaginatedResponse chung của app
        return new AdminPaginatedResponse<>(
                dtoList,
                cinemaPage.getNumber(),
                cinemaPage.getTotalPages(),
                cinemaPage.getTotalElements(),
                cinemaPage.isLast()
        );
    }

    // Hàm tiện ích để Map Entity -> DTO
    private AdminCinemaDto mapToDto(Cinema cinema) {
        return AdminCinemaDto.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .address(cinema.getAddress())
                .description(cinema.getDescription())
                .region(cinema.getRegion())
                .cineplex(cinema.getCineplex())
                .latitude(cinema.getLatitude())
                .longitude(cinema.getLongitude())
                .logoUrl(cinema.getLogoUrl())
                .isActive(cinema.getIsActive())
                .build();
    }


    public List<String> getAvailableRegions() {
        return cinemaRepository.findDistinctRegions();
    }

    public List<String> getAvailableCineplexes() {
        return cinemaRepository.findDistinctCineplexes();
    }

    @Transactional
    public AdminCinemaDto createCinema(AdminCinemaCreateRequest request, MultipartFile logo) {

        // 1. Upload Logo lên Cloudinary (nếu có)
        String logoUrl = null;
        if (logo != null && !logo.isEmpty()) {
            logoUrl = cloudinaryService.uploadImage(logo);
        }

        // 2. Khởi tạo Entity Cinema
        Cinema cinema = Cinema.builder()
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .region(request.getRegion())
                .cineplex(request.getCineplex())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                // Nếu client không gửi isActive, mặc định là true
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .logoUrl(logoUrl)
                .build();

        // 3. Lưu xuống Database
        Cinema savedCinema = cinemaRepository.save(cinema);

        generateDefaultRoomsForCinema(savedCinema.getId());

        return mapToDto(savedCinema);
    }

    // Tách riêng logic tạo phòng mặc định cho gọn code
    private void generateDefaultRoomsForCinema(java.util.UUID cinemaId) {
        for (int i = 1; i <= 6; i++) {
            CreateRoomRequest roomRequest = new CreateRoomRequest();

            roomRequest.setCinemaId(cinemaId);
            roomRequest.setName("Room " + i);
            roomRequest.setRows(11); // 11 Hàng ghế (Từ A đến K)
            roomRequest.setSeatsPerRow(10); // 10 ghế mỗi hàng

            // Cấu hình hàng VIP
            roomRequest.setVipRows(List.of(6, 7, 8, 9, 10)); // Hàng F, G, H, I, J

            // Cấu hình hàng Couple
            roomRequest.setCoupleRows(List.of(11)); // Hàng K

            // Gọi logic của RoomService để tạo Room và Seat
            roomService.createRoom(roomRequest);
        }
    }

    public AdminCinemaDto getCinemaById(UUID id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp với ID: " + id));

        return mapToDto(cinema);
    }

    @Transactional
    public AdminCinemaDto updateCinema(UUID id, AdminCinemaUpdateRequest request, MultipartFile logo) {
        // Tìm rạp hiện tại trong DB
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp với ID: " + id));

        // Nếu người dùng chọn ảnh mới (logo gửi lên không null) -> Upload và set URL mới
        if (logo != null && !logo.isEmpty()) {
            String newLogoUrl = cloudinaryService.uploadImage(logo);
            cinema.setLogoUrl(newLogoUrl);
        }
        // Lưu ý: Nếu logo == null, URL ảnh cũ vẫn được giữ nguyên trong DB

        // Cập nhật các trường thông tin chữ
        cinema.setName(request.getName());
        cinema.setAddress(request.getAddress());
        cinema.setDescription(request.getDescription());
        cinema.setRegion(request.getRegion());
        cinema.setCineplex(request.getCineplex());
        cinema.setLatitude(request.getLatitude());
        cinema.setLongitude(request.getLongitude());

        if (request.getIsActive() != null) {
            cinema.setIsActive(request.getIsActive());
        }

        // Lưu xuống DB và trả về DTO
        Cinema savedCinema = cinemaRepository.save(cinema);
        return mapToDto(savedCinema);
    }


}