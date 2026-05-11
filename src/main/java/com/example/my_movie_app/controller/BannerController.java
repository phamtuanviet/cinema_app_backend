package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.BannerDto;
import com.example.my_movie_app.entity.Banner;
import com.example.my_movie_app.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/active")
    public List<BannerDto> getActive() {
        return bannerService.getActiveBanners();
    }
    @GetMapping
    public List<Banner> getAll() {
        return bannerService.getAll();
    }

    @PostMapping
    public Banner create(@RequestBody Banner banner) {
        return bannerService.create(banner);
    }

    @PutMapping("/{id}")
    public Banner update(@PathVariable UUID id, @RequestBody Banner banner) {
        return bannerService.update(id, banner);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        bannerService.delete(id);
    }
}