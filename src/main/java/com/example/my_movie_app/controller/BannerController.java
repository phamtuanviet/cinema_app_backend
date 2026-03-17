package com.example.my_movie_app.controller;

import com.example.my_movie_app.entity.Banner;
import com.example.my_movie_app.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // 📱 API cho mobile (chỉ lấy banner active)
    @GetMapping("/active")
    public List<Banner> getActive() {
        return bannerService.getActiveBanners();
    }

    // 📄 Admin - lấy tất cả
    @GetMapping
    public List<Banner> getAll() {
        return bannerService.getAll();
    }

    // ➕ Create
    @PostMapping
    public Banner create(@RequestBody Banner banner) {
        return bannerService.create(banner);
    }

    // ✏️ Update
    @PutMapping("/{id}")
    public Banner update(@PathVariable UUID id, @RequestBody Banner banner) {
        return bannerService.update(id, banner);
    }

    // ❌ Delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        bannerService.delete(id);
    }
}