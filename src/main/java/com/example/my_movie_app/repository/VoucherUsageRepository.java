package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, UUID> {
    List<VoucherUsage> findByBooking(Booking booking);
}