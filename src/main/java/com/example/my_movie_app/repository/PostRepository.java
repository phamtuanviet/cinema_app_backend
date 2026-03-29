package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Post;
import com.example.my_movie_app.enums.PostType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByPublishedTrueOrderByCreatedAtDesc();

    Optional<Post> findByIdAndPublishedTrue(UUID id);


    List<Post> findByType(PostType type);

    boolean existsByTitle(String title);

    @Query("""
        SELECT p FROM Post p
        WHERE p.published = true
        AND p.type = :type
        AND (p.startDate IS NULL OR p.startDate <= CURRENT_TIMESTAMP)
        AND (p.endDate IS NULL OR p.endDate >= CURRENT_TIMESTAMP)
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findActivePostsByType(
            @Param("type") PostType type,
            Pageable pageable
    );
}