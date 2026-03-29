package com.example.my_movie_app.dto.request;

import com.example.my_movie_app.enums.PostType;
import lombok.Data;

import java.util.UUID;

@Data
public class PostRequest {
    private String title;
    private String content;
    private String thumbnailUrl;
    private PostType type;
    private UUID voucherId;
}
