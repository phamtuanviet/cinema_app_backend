package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.projection.CinemaProjection;
import com.example.my_movie_app.projection.RegionProjection;
import io.lettuce.core.dynamic.annotation.Param;
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
}