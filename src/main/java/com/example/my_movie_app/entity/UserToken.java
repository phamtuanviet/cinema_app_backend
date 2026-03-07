package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserToken extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String refreshToken;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isRevoked = false;

    private String deviceInfo;

    private String ipAddress;

}