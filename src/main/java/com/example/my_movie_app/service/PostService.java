package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.VoucherPostDto;
import com.example.my_movie_app.dto.request.PostRequest;
import com.example.my_movie_app.dto.response.PostDetailResponse;
import com.example.my_movie_app.dto.response.PostResponse;
import com.example.my_movie_app.entity.Post;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.enums.PostType;
import com.example.my_movie_app.repository.PostRepository;
import com.example.my_movie_app.repository.VoucherRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final VoucherRepository voucherRepository;
    private final ObjectMapper objectMapper;

    public Page<PostResponse> getPostsByType(PostType type, Pageable pageable) {

        Page<Post> posts = postRepository.findActivePostsByType(type, pageable);

        return posts.map(this::mapToResponse);
    }

    private PostResponse mapToResponse(Post post) {

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .thumbnailUrl(post.getThumbnailUrl())
                .type(post.getType())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .build();
    }

    public int importPosts(MultipartFile file) {
        try {

            List<Post> posts = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<Post>>() {}
            );

            int count = 0;
            for (Post post : posts) {
                if (postRepository.existsByTitle(post.getTitle())) continue;

                LocalDateTime now = LocalDateTime.now();

                post.setStartDate(now);
                post.setEndDate(now.plusDays(15));

                post.setPublished(true);

                postRepository.save(post);
                count++;
            }

            return count;

        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    public PostDetailResponse getPostById(UUID id) {
        Post post = postRepository.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        return mapToDetailResponse(post);
    }

    private PostDetailResponse mapToDetailResponse(Post post) {
        PostDetailResponse res = new PostDetailResponse();

        res.setId(post.getId());
        res.setTitle(post.getTitle());
        res.setContent(post.getContent());
        res.setThumbnailUrl(post.getThumbnailUrl());
        res.setPublished(post.getPublished());
        res.setType(post.getType());
        res.setStartDate(post.getStartDate());
        res.setEndDate(post.getEndDate());

        // ===== isActive =====
        LocalDateTime now = LocalDateTime.now();
        boolean isActive = Boolean.TRUE.equals(post.getPublished())
                && (post.getStartDate() == null || !now.isBefore(post.getStartDate()))
                && (post.getEndDate() == null || !now.isAfter(post.getEndDate()));

        res.setIsActive(isActive);

        // ===== voucher =====
        if (post.getVoucher() != null) {
            res.setVoucher(mapVoucher(post.getVoucher()));
        }

        return res;
    }

    private VoucherPostDto mapVoucher(Voucher v) {
        VoucherPostDto dto = new VoucherPostDto();

        dto.setCode(v.getCode());
        dto.setDiscountType(v.getDiscountType());
        dto.setDiscountValue(v.getDiscountValue());
        dto.setMinOrderValue(v.getMinOrderValue());
        dto.setMaxDiscount(v.getMaxDiscount());
        dto.setExpiryDate(v.getExpiryDate());
        dto.setActive(v.getActive());
        dto.setUsageLimit(v.getUsageLimit());
        dto.setUsedCount(v.getUsedCount());

        if (v.getUsageLimit() != null) {
            dto.setRemainingUsage(v.getUsageLimit() - v.getUsedCount());
        }

        return dto;
    }
}