package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.enums.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(:search IS NULL OR " +
            "LOWER(u.email) LIKE :search OR " +
            "LOWER(u.fullName) LIKE :search OR " +
            "LOWER(u.phone) LIKE :search)")
    Page<User> searchUsersByRole(
            @Param("search") String search,
            @Param("role") Role role,
            Pageable pageable
    );
}