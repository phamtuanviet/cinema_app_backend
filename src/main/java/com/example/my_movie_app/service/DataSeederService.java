package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.request.BannerRequest;
import com.example.my_movie_app.dto.request.MovieRequest;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataSeederService {

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BannerRepository bannerRepository;
    private final GenreRepository genreRepository;
    private final ObjectMapper objectMapper;


    private static final int BUFFER_MINUTES = 15;

    public void generateShowtimes(int startDay, int endDay) {

        List<Movie> movies = movieRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        if (movies.isEmpty() || rooms.isEmpty()) {
            throw new RuntimeException("Thiếu dữ liệu");
        }

        List<LocalTime> timeSlots = List.of(
                LocalTime.of(8, 0),
                LocalTime.of(10, 30),
                LocalTime.of(13, 0),
                LocalTime.of(15, 30),
                LocalTime.of(18, 0),
                LocalTime.of(20, 30)
        );

        LocalDate startDate = LocalDate.now();

        for (Movie movie : movies) {

            List<Room> selectedRooms = getRandomRooms(rooms, Math.min(10, rooms.size()));

            for (Room room : selectedRooms) {

                for (int d = startDay; d <= endDay; d++) {
                    LocalDate date = startDate.plusDays(d);

                    for (LocalTime time : timeSlots) {

                        LocalDateTime startTime = LocalDateTime.of(date, time);

                        LocalDateTime endTime = startTime
                                .plusMinutes(movie.getDurationMinutes())
                                .plusMinutes(15);

                        boolean conflict = showtimeRepository
                                .existsByRoomAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                        room,
                                        "ACTIVE",
                                        endTime,
                                        startTime
                                );

                        if (conflict) continue;

                        BigDecimal basePrice = new BigDecimal("80000");

                        BigDecimal weekendModifier = isWeekend(date.getDayOfWeek())
                                ? new BigDecimal("10000")
                                : BigDecimal.ZERO;

                        Showtime showtime = Showtime.builder()
                                .movie(movie)
                                .room(room)
                                .startTime(startTime)
                                .endTime(endTime)
                                .basePrice(basePrice.add(weekendModifier))
                                .weekendModifier(weekendModifier)
                                .status("ACTIVE")
                                .build();

                        showtimeRepository.save(showtime);
                    }
                }
            }
        }

        System.out.println("Optimized showtime generated!");
    }

    // ================= HELPER =================

    private List<Room> getRandomRooms(List<Room> rooms, int count) {
        List<Room> shuffled = new ArrayList<>(rooms);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    private boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }


    public void importBannersFromJson(InputStream inputStream) {
        try {
            List<BannerRequest> requests = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<BannerRequest>>() {}
            );

            List<Banner> banners = new ArrayList<>();

            for (BannerRequest req : requests) {

                Banner banner = Banner.builder()
                        .imageUrl(req.getImageUrl())
                        .actionType(req.getActionType())
                        .actionValue(req.getActionValue())
                        .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                        .priority(req.getPriority() != null ? req.getPriority() : 0)
                        .build();

                banners.add(banner);
            }

            // 🚀 save batch
            bannerRepository.saveAll(banners);

        } catch (Exception e) {
            throw new RuntimeException("Import banner failed: " + e.getMessage());
        }
    }

    private String normalize(String name) {
        return name.trim().toLowerCase();
    }

    private String capitalize(String name) {
        return Arrays.stream(name.split(" "))
                .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    @Transactional
    public void importMoviesFromJson(InputStream inputStream) {
        try {
            List<MovieRequest> requests = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<MovieRequest>>() {}
            );

            // 🔥 1. Lấy toàn bộ genre name từ JSON
            Set<String> genreNames = requests.stream()
                    .flatMap(m -> m.getGenres().stream())
                    .map(g -> normalize(g.getName()))
                    .collect(Collectors.toSet());

            // 🔥 2. Query 1 lần tất cả genre trong DB
            List<Genre> existingGenres = genreRepository.findAll();

            Map<String, Genre> genreMap = new HashMap<>();
            for (Genre g : existingGenres) {
                genreMap.put(normalize(g.getName()), g);
            }

            // 🔥 3. Tạo genre chưa có (batch)
            List<Genre> newGenres = new ArrayList<>();

            for (String name : genreNames) {
                if (!genreMap.containsKey(name)) {
                    Genre g = Genre.builder()
                            .name(capitalize(name))
                            .build();
                    newGenres.add(g);
                    genreMap.put(name, g);
                }
            }

            if (!newGenres.isEmpty()) {
                genreRepository.saveAll(newGenres);
            }

            // 🔥 4. Build movie list (không save từng cái)
            List<Movie> movies = new ArrayList<>();

            for (MovieRequest req : requests) {

                List<Genre> genres = req.getGenres().stream()
                        .map(g -> genreMap.get(normalize(g.getName())))
                        .collect(Collectors.toList());

                if (movieRepository.existsByTitleAndReleaseDate(req.getTitle(), req.getReleaseDate())) {
                    continue;
                }

                Movie movie = Movie.builder()
                        .title(req.getTitle().trim())
                        .description(req.getDescription())
                        .durationMinutes(req.getDurationMinutes())
                        .releaseDate(req.getReleaseDate())
                        .posterUrl(req.getPosterUrl())
                        .trailerUrl(req.getTrailerUrl())
                        .ageRating(req.getAgeRating())
                        .language(req.getLanguage())
                        .isActive(req.getIsActive())
                        .genres(genres)
                        .build();

                movies.add(movie);
            }

            // 🔥 5. Save ALL (batch insert)
            movieRepository.saveAll(movies);

        } catch (Exception e) {
            throw new RuntimeException("Import failed", e);
        }
    }
}