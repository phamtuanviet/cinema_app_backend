package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.entity.UserToken;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.entity.Voucher;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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


    List<UserVoucher> findByUserIdAndIsUsed(UUID userId, Boolean isUsed);


    @Query("""
    SELECT uv FROM UserVoucher uv
    JOIN FETCH uv.voucher v
    WHERE uv.user.id = :userId 
      AND uv.isUsed = false 
      AND v.active = true 
      AND v.expiryDate > CURRENT_TIMESTAMP
""")
    List<UserVoucher> findValidAndUnusedByUserId(@Param("userId") UUID userId);




}