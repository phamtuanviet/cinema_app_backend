package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.AdminNewsDto;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.dto.AdminPostCreateRequest;
import com.example.my_movie_app.dto.AdminPostUpdateRequest;
import com.example.my_movie_app.entity.Post;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.enums.PostType;
import com.example.my_movie_app.repository.PostRepository;
import com.example.my_movie_app.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNewsService {

    private final PostRepository postRepository;
    private final VoucherRepository voucherRepository;
    private final CloudinaryService cloudinaryService; // Inject service upload ảnh
    private final FcmService fcmService;

    public AdminPaginatedResponse<AdminNewsDto> getNews(String search, String typeStr, int page, int size) {

        // 1. Chuẩn hóa chuỗi tìm kiếm (Chữ in hoa, bọc % và khởi tạo rỗng để fix lỗi PostgreSQL bytea)
        String searchParam = "";
        if (search != null && !search.trim().isEmpty()) {
            searchParam = "%" + search.trim().toUpperCase() + "%";
        }

        // 2. Chuyển đổi Enum Loại bài viết (NORMAL hoặc VOUCHER)
        PostType targetType;
        try {
            targetType = PostType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            targetType = PostType.NORMAL; // Mặc định hiển thị Tin tức bình thường
        }

        // 3. Phân trang & Sắp xếp (Bài mới nhất lên đầu - giả định BaseEntity có createdAt)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 4. Truy vấn DB
        Page<Post> postPage = postRepository.searchPostsByType(searchParam, targetType, pageable);

        // 5. Ánh xạ Entity -> DTO
        List<AdminNewsDto> dtoList = postPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new AdminPaginatedResponse<>(
                dtoList,
                postPage.getNumber(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isLast()
        );
    }

    // Hàm Map dùng chung
    public AdminNewsDto mapToDto(Post post) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

        String startDateStr = null;
        if (post.getStartDate() != null) {
            startDateStr = post.getStartDate().format(formatter);
        }

        String endDateStr = null;
        if (post.getEndDate() != null) {
            endDateStr = post.getEndDate().format(formatter);
        }

        // Rút trích an toàn mã Voucher nếu có
        String vCode = null;
        if (post.getVoucher() != null) {
            vCode = post.getVoucher().getCode();
        }

        return AdminNewsDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .thumbnailUrl(post.getThumbnailUrl())
                .published(post.getPublished())
                .type(post.getType() != null ? post.getType().name() : "NORMAL")
                .startDate(startDateStr)
                .endDate(endDateStr)
                .voucherCode(vCode)
                .content(post.getContent())
                .build();
    }




    // ... (Hàm getNews và mapToDto cũ giữ nguyên)

    // 1. API Lấy chi tiết bài viết đổ lên Form
    public AdminNewsDto getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
        return mapToDto(post);
    }

    // 2. API Cập nhật bài viết
    @Transactional
    public AdminNewsDto updatePost(UUID id, AdminPostUpdateRequest request, MultipartFile thumbnail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết với ID: " + id));

        // Cập nhật thông tin cơ bản
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getPublished() != null) post.setPublished(request.getPublished());

        // Parse Loại bài viết
        try {
            if (request.getType() != null) {
                post.setType(PostType.valueOf(request.getType().toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            post.setType(PostType.NORMAL);
        }

        // Xử lý Liên kết Voucher
        if (post.getType() == PostType.VOUCHER && request.getVoucherId() != null) {
            // Tìm voucher trong DB và gán vào bài viết
            var voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
            post.setVoucher(voucher);
        } else {
            // Nếu là NORMAL hoặc không chọn -> Cắt đứt liên kết cũ (nếu có)
            post.setVoucher(null);
        }

        // Parse Ngày tháng
        post.setStartDate(parseIsoDate(request.getStartDate()));
        post.setEndDate(parseIsoDate(request.getEndDate()));

        // Xử lý Upload Ảnh Thumbnail
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                String newThumbnailUrl = cloudinaryService.uploadImage(thumbnail);
                post.setThumbnailUrl(newThumbnailUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi upload ảnh Thumbnail: " + e.getMessage());
            }
        }

        Post savedPost = postRepository.save(post);
        return mapToDto(savedPost);
    }

    // Helper method để parse chuỗi ISO an toàn
    private LocalDateTime parseIsoDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            return null; // Bỏ qua nếu parse lỗi
        }
    }

    @Transactional
    public AdminNewsDto createPost(AdminPostCreateRequest request, MultipartFile thumbnail) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPublished(request.getPublished() != null ? request.getPublished() : true);

        // Set Type
        try {
            post.setType(PostType.valueOf(request.getType().toUpperCase()));
        } catch (Exception e) {
            post.setType(PostType.NORMAL);
        }

        // Link Voucher
        if (post.getType() == PostType.VOUCHER && request.getVoucherId() != null) {
            Voucher v = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
            post.setVoucher(v);
        }

        // Parse Date & Upload Image (Tương tự phần Update)
        post.setStartDate(parseIsoDate(request.getStartDate()));
        post.setEndDate(parseIsoDate(request.getEndDate()));

        if (thumbnail != null && !thumbnail.isEmpty()) {
            post.setThumbnailUrl(cloudinaryService.uploadImage(thumbnail));
        }

        Post saved = postRepository.save(post);
        if (Boolean.TRUE.equals(request.getSendNotification())) {
                String title = "🔥 HOT: " + saved.getTitle();
                String body = "Mở app ngay để xem chi tiết thông báo mới nhất từ chúng tôi!";

                Map<String, String> data = new HashMap<>();
                data.put("action", "OPEN_NEWS_DETAIL");
                data.put("newsId", saved.getId().toString());

                fcmService.sendGlobalNotificationByTopic("ALL_USERS", title, body, data);

        }
        return mapToDto(saved);
    }
}