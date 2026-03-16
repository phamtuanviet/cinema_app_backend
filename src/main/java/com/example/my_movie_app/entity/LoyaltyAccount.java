package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "loyalty_accounts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccount {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    private User user;

    private Integer availablePoints = 0;
}