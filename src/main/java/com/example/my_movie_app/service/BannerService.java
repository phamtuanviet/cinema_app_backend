package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.BannerDto;
import com.example.my_movie_app.entity.Banner;
import com.example.my_movie_app.enums.BannerActionType;
import com.example.my_movie_app.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<BannerDto> getActiveBanners() {
        return bannerRepository.findTop4ByIsActiveTrueOrderByPriorityDescCreatedAtDesc()
                .stream()
                .map(b -> new BannerDto(
                        b.getId(),
                        b.getImageUrl(),
                        b.getActionType() != null ? b.getActionType().name() : null,
                        b.getTargetUrl(),
                        b.getMovieId(),
                        b.getPriority()
                ))
                .collect(Collectors.toList());
    }

    public Banner create(Banner banner) {
        // Logic làm sạch dữ liệu trước khi lưu
        cleanUpBannerData(banner);
        return bannerRepository.save(banner);
    }

    public Banner update(UUID id, Banner request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        banner.setImageUrl(request.getImageUrl());
        banner.setActionType(request.getActionType());
        banner.setTargetUrl(request.getTargetUrl());
        banner.setMovieId(request.getMovieId()); // Bổ sung movieId
        banner.setIsActive(request.getIsActive());
        banner.setPriority(request.getPriority());

        // Logic làm sạch dữ liệu trước khi lưu
        cleanUpBannerData(banner);

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

    // 🛡️ Hàm helper: Làm sạch dữ liệu để tránh lưu rác (ví dụ: action là URL nhưng vẫn dính movieId cũ)
    private void cleanUpBannerData(Banner banner) {
        if (banner.getActionType() == BannerActionType.URL) {
            banner.setMovieId(null);
        } else if (banner.getActionType() == BannerActionType.MOVIE) {
            banner.setTargetUrl(null);
        }
    }
}