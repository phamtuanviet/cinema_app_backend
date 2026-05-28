package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Banner;
import com.example.my_movie_app.enums.BannerActionType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {
    @Query("SELECT b FROM Banner b LEFT JOIN Movie m ON b.movieId = m.id WHERE " +
            "b.actionType = :actionType AND " +
            "(:search = '' OR " +
            "  (b.actionType = 'MOVIE' AND UPPER(m.title) LIKE UPPER(:search)) OR " +
            "  (b.actionType = 'URL' AND UPPER(b.targetUrl) LIKE UPPER(:search))" +
            ")")
    Page<Banner> searchBanners(
            @Param("search") String search,
            @Param("actionType") BannerActionType actionType,
            Pageable pageable
    );

    List<Banner> findByIsActiveTrueOrderByPriorityDesc();

    List<Banner> findTop4ByIsActiveTrueOrderByPriorityDescCreatedAtDesc();
}