package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "loyalty_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer points;

    @Enumerated(EnumType.STRING)
    private LoyaltyTransactionType type;

}