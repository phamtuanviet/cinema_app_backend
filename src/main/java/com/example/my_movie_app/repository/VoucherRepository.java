package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    List<Voucher> findByActiveTrueAndExpiryDateAfter(LocalDateTime now);

    Optional<Voucher> findByCode(String code);

}
