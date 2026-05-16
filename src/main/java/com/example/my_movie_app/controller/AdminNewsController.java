package com.example.my_movie_app.controller;


import com.example.my_movie_app.dto.AdminNewsDto;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.dto.AdminPostCreateRequest;
import com.example.my_movie_app.dto.AdminPostUpdateRequest;
import com.example.my_movie_app.service.AdminNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminNewsController {

    private final AdminNewsService adminNewsService;

    @GetMapping
    public AdminPaginatedResponse<AdminNewsDto> getNews(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NORMAL") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminNewsService.getNews(search, type, page, size);
    }

    @GetMapping("/{id}")
    public AdminNewsDto getPostById(@PathVariable UUID id) {
        return adminNewsService.getPostById(id);
    }

    // Cập nhật bài viết (Hứng file ảnh + JSON)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminNewsDto updatePost(
            @PathVariable UUID id,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart("data") AdminPostUpdateRequest request
    ) {
        return adminNewsService.updatePost(id, request, thumbnail);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminNewsDto createPost(
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart("data") AdminPostCreateRequest request
    ) {
        return adminNewsService.createPost(request, thumbnail);
    }
}