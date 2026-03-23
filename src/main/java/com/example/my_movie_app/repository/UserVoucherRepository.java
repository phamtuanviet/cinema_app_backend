package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.entity.UserToken;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, UUID> {



    List<UserVoucher> findByUserId(UUID userId);

    boolean existsByUserIdAndVoucherId(UUID userId, UUID voucherId);

    Optional<UserVoucher> findByUser_IdAndVoucher_IdAndIsUsedFalse(
            UUID userId,
            UUID voucherId
    );
}