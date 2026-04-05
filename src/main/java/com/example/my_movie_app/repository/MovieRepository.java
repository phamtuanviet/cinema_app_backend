package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Movie;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"genres"})
    List<Movie> findAllByIdIn(List<UUID> ids);

    List<Movie> findByTitleContainingIgnoreCase(String keyword);

    boolean existsByTitleAndReleaseDate(String title, LocalDate releaseDate);

    @EntityGraph(attributePaths = {"genres"})
    Optional<Movie> findById(UUID id);

    List<Movie> findByIsActiveTrueAndReleaseDateBefore(LocalDate date); // now showing
    List<Movie> findByIsActiveTrueAndReleaseDateAfter(LocalDate date);

    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.genres g " +
            "JOIN Showtime s ON s.movie = m " +
            "WHERE m.isActive = true " +
            "AND s.startTime >= :now " +
            // Thay IS NULL bằng = '' cho keyword và genre
            "AND (:keyword = '' OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:genre = '' OR LOWER(g.name) LIKE LOWER(CONCAT('%', :genre, '%'))) " +
            // language và ageRating so sánh trực tiếp, không qua LOWER() nên vẫn dùng IS NULL bình thường
            "AND (:language IS NULL OR m.language = :language) " +
            "AND (:ageRating IS NULL OR m.ageRating = :ageRating)")
    Page<Movie> searchMoviesWithActiveShowtimes(
            @Param("keyword") String keyword,
            @Param("genre") String genre,
            @Param("language") String language,
            @Param("ageRating") String ageRating,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN Showtime s ON s.movie = m " +
            "JOIN s.room r " +
            "JOIN r.cinema c " +
            "WHERE m.isActive = true " +
            "AND (:loc = '' OR (" +
            "   LOWER(c.region) LIKE LOWER(CONCAT('%', :loc, '%')) OR " +
            "   LOWER(c.cineplex) LIKE LOWER(CONCAT('%', :loc, '%')) OR " +
            "   LOWER(c.address) LIKE LOWER(CONCAT('%', :loc, '%')) OR " +
            "   LOWER(c.description) LIKE LOWER(CONCAT('%', :loc, '%')) " +
            ")) " +
            "AND s.startTime >= :fromDate " +
            // THÊM CAST Ở ĐÂY: Giúp Postgres xác định kiểu dữ liệu kể cả khi toDate là null
            "AND (CAST(:toDate AS LocalDateTime) IS NULL OR s.startTime <= :toDate)")
    Page<Movie> getNowPlayingMovies(
            @Param("loc") String loc,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("SELECT m FROM Movie m " +
            "WHERE m.isActive = true " +
            "AND m.releaseDate > :today " +
            "ORDER BY m.releaseDate ASC")
    Page<Movie> getComingSoonMovies(@Param("today") LocalDate today, Pageable pageable);

    @Query("SELECT m FROM Movie m " +
            // Chặn lỗi nếu title là rỗng
            "WHERE (:title = '' OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            // Year là số nguyên, giữ nguyên IS NULL
            "AND (:year IS NULL OR EXTRACT(YEAR FROM m.releaseDate) = :year) " +
            // Đổi IS NULL thành kiểm tra chuỗi rỗng cho language
            "AND (:language = '' OR LOWER(m.language) = LOWER(:language)) " +
            "ORDER BY m.releaseDate DESC") // Ưu tiên phần mới nhất lên đầu
    List<Movie> findMovieDetailForChatbot(
            @Param("title") String title,
            @Param("year") Integer year,
            @Param("language") String language
    );

}