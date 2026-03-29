package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.request.PostRequest;
import com.example.my_movie_app.dto.response.PostDetailResponse;
import com.example.my_movie_app.dto.response.PostResponse;
import com.example.my_movie_app.enums.PostType;
import com.example.my_movie_app.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/import")
    public ResponseEntity<?> importPosts(@RequestParam("file") MultipartFile file) {

        int count = postService.importPosts(file);

        return ResponseEntity.ok("Imported " + count + " posts");
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam PostType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                postService.getPostsByType(type, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPostById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }
}