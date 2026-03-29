package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String fullName;
    private String phone;

    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private String avatarUrl;
}