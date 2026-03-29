package com.example.my_movie_app.entity;

import com.example.my_movie_app.enums.PostType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnailUrl;

    private Boolean published = true;

    @Enumerated(EnumType.STRING)
    private PostType type;


    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
}