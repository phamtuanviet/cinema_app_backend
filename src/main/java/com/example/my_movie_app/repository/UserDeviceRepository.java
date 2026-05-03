package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

    // Dùng lúc Login để tìm token có sẵn
    Optional<UserDevice> findByFcmToken(String fcmToken);

    // Dùng lúc Gửi Thông Báo (Lấy tất cả các thiết bị đang active của 1 user)
    List<UserDevice> findAllByUserIdAndActiveTrue(UUID userId);
}