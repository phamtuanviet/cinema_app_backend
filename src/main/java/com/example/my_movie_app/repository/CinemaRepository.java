package com.example.my_movie_app.repository;

import com.example.my_movie_app.dto.response.CinemaNearbyResponse;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.projection.CinemaProjection;
import com.example.my_movie_app.projection.RegionProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CinemaRepository extends JpaRepository<Cinema, UUID> {
    List<Cinema> findByIsActiveTrue();

    List<Cinema> findByRegion(String region);

    List<Cinema> findByLatitudeAndLongitude(Double latitude, Double longitude);

    Optional<Cinema> findByName(String name);

    @Query(value = """
SELECT * FROM (
    SELECT 
        c.id, 
        c.name, 
        c.address, 
        c.latitude, 
        c.longitude,
        (
            6371 * acos(
                LEAST(1, GREATEST(-1,
                    cos(radians(:lat)) *
                    cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lng)) +
                    sin(radians(:lat)) *
                    sin(radians(c.latitude))
                ))
            )
        ) AS distance, 
        c.logo_url
    FROM cinemas c
    WHERE c.is_active = true
) AS sub
WHERE sub.distance <= :radius
ORDER BY sub.distance
LIMIT 15
""", nativeQuery = true)
    List<CinemaProjection> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius
    );

    // 🔥 2. Region list
    @Query("""
        SELECT c.region as region, COUNT(c) as totalCinema
        FROM Cinema c
        WHERE c.isActive = true
        GROUP BY c.region
    """)
    List<RegionProjection> getRegions();

    @Query(value = """
    SELECT * FROM (
        SELECT 
            c.id, 
            c.name, 
            c.address, 
            c.latitude, 
            c.longitude,
            (
                6371 * acos(
                    cos(radians(:lat)) *
                    cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lng)) +
                    sin(radians(:lat)) *
                    sin(radians(c.latitude))
                )
            ) AS distance, 
            c.logo_url
        FROM cinemas c
        WHERE c.region = :region
        AND c.is_active = true
    ) AS sub
    ORDER BY sub.distance
""", nativeQuery = true)
    List<CinemaProjection> findByRegionWithDistance(
            @Param("region") String region,
            @Param("lat") double lat,
            @Param("lng") double lng
    );

    @Query(value = "SELECT new com.example.my_movie_app.dto.response.CinemaNearbyResponse(" +
            "c.id, c.name, c.address, c.cineplex, " +
            // Dùng cờ hasLocation để quyết định có tính khoảng cách hay trả về 0.0
            "CASE WHEN :hasLocation = true " +
            "THEN (6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * cos(radians(c.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(c.latitude)))) " +
            "ELSE 0.0 END, " +
            "c.logoUrl) " +
            "FROM Cinema c " +
            "WHERE c.isActive = true " +

            // Bỏ qua lọc khoảng cách nếu hasLocation = false
            "AND (:hasLocation = false OR (6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * cos(radians(c.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(c.latitude)))) <= :radius) " +

            "AND (COALESCE(:query, '') = '' OR (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.cineplex) LIKE LOWER(CONCAT('%', :query, '%')))) " +

            "AND (COALESCE(:movieTitle, '') = '' OR EXISTS (" +
            "    SELECT 1 FROM Showtime s " +
            "    JOIN s.movie m " +
            "    WHERE s.room.cinema.id = c.id " +
            "    AND s.startTime >= CURRENT_TIMESTAMP " +
            "    AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :movieTitle, '%')) " +
            "         OR LOWER(m.description) LIKE LOWER(CONCAT('%', :movieTitle, '%'))) " +
            ")) " +

            "ORDER BY 5 ASC")
    List<CinemaNearbyResponse> findCinemasNearby(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius,
            @Param("movieTitle") String movieTitle,
            @Param("query") String query,
            @Param("hasLocation") boolean hasLocation // Thêm cờ này
    );

    Page<Cinema> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 🔥 Lấy danh sách Khu vực (Region) không trùng lặp và loại bỏ các dòng rỗng/null
    @Query("SELECT DISTINCT c.region FROM Cinema c WHERE c.region IS NOT NULL AND trim(c.region) <> '' ORDER BY c.region")
    List<String> findDistinctRegions();

    // 🔥 Lấy danh sách Cụm rạp (Cineplex) không trùng lặp và loại bỏ các dòng rỗng/null
    @Query("SELECT DISTINCT c.cineplex FROM Cinema c WHERE c.cineplex IS NOT NULL AND trim(c.cineplex) <> '' ORDER BY c.cineplex")
    List<String> findDistinctCineplexes();

    List<Cinema> findTop10ByNameContainingIgnoreCase(String name);
}