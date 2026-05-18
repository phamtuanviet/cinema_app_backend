package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Voucher;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    List<Voucher> findByActiveTrueAndExpiryDateAfter(LocalDateTime now);

    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE " +
            // 🔥 SỬA Ở ĐÂY: Check chuỗi rỗng và bỏ UPPER(:search)
            "(:search = '' OR UPPER(v.code) LIKE :search) AND " +
            "( " +
            "  (:status = 'VALID' AND v.active = true " +
            "    AND (v.expiryDate IS NULL OR v.expiryDate > :now) " +
            "    AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)) " +
            "  OR " +
            "  (:status = 'INVALID' AND (" +
            "    v.active = false " +
            "    OR (v.expiryDate IS NOT NULL AND v.expiryDate <= :now) " +
            "    OR (v.usageLimit IS NOT NULL AND v.usedCount >= v.usageLimit)" +
            "  )) " +
            ")")
    Page<Voucher> searchVouchersByStatus(
            @Param("search") String search,
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 " +
            "WHERE v.id = :voucherId AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    int incrementUsedCountIfAvailable(@Param("voucherId") UUID voucherId);

    boolean existsByCode(String code);

}
