package com.example.my_movie_app.service;

import com.example.my_movie_app.entity.Banner;
import com.example.my_movie_app.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<Banner> getActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByPriorityDesc();
    }

    public Banner create(Banner banner) {
        return bannerRepository.save(banner);
    }

    public Banner update(UUID id, Banner request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        banner.setImageUrl(request.getImageUrl());
        banner.setActionType(request.getActionType());
        banner.setActionValue(request.getActionValue());
        banner.setIsActive(request.getIsActive());
        banner.setPriority(request.getPriority());

        return bannerRepository.save(banner);
    }

    // ❌ Delete
    public void delete(UUID id) {
        bannerRepository.deleteById(id);
    }

    // 📄 Lấy tất cả (admin)
    public List<Banner> getAll() {
        return bannerRepository.findAll();
    }
}