package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.CinemaDto;
import com.example.my_movie_app.dto.MovieShowtimeDto;
import com.example.my_movie_app.dto.RegionDto;
import com.example.my_movie_app.dto.ShowtimeDto;
import com.example.my_movie_app.dto.response.CinemaShowtimeResponse;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.entity.Genre;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.CinemaRepository;
import com.example.my_movie_app.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final ShowtimeRepository showtimeRepository;

    public List<Cinema> getAllCinemas() {
        return cinemaRepository.findAll();
    }

    public List<Cinema> getActiveCinemas() {
        return cinemaRepository.findByIsActiveTrue();
    }

    private double round1Decimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public Cinema getCinemaById(UUID id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cinema not found"));
    }

    public List<Cinema> getCinemasByRegion(String region) {
        return cinemaRepository.findByRegion(region);
    }

    public Cinema createCinema(Cinema cinema) {
        return cinemaRepository.save(cinema);
    }

    public Cinema updateCinema(UUID id, Cinema cinema) {

        Cinema existing = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cinema not found"));

        existing.setName(cinema.getName());
        existing.setAddress(cinema.getAddress());
        existing.setDescription(cinema.getDescription());
        existing.setRegion(cinema.getRegion());
        existing.setCineplex(cinema.getCineplex());
        existing.setLatitude(cinema.getLatitude());
        existing.setLongitude(cinema.getLongitude());
        existing.setIsActive(cinema.getIsActive());

        return cinemaRepository.save(existing);
    }

    public void deleteCinema(UUID id) {
        cinemaRepository.deleteById(id);
    }

    public List<CinemaDto> getNearby(double lat, double lng, double radius) {
        List<CinemaDto> result = cinemaRepository.findNearby(lat, lng,radius)
                .stream()
                .map(p -> new CinemaDto(
                        p.getId(),
                        p.getName(),
                        p.getAddress(),
                        round1Decimal(p.getDistance()),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getLogoUrl()
                ))
                .toList();
        System.out.println(result);
        return result;
    }

    public List<RegionDto> getRegions() {
        return cinemaRepository.getRegions()
                .stream()
                .map(r -> new RegionDto(
                        r.getRegion(),
                        r.getTotalCinema()
                ))
                .toList();
    }

    public List<CinemaDto> getByRegion(String region, double lat, double lng) {
        return cinemaRepository.findByRegionWithDistance(region, lat, lng)
                .stream()
                .map(p -> new CinemaDto(
                        p.getId(),
                        p.getName(),
                        p.getAddress(),
                        round1Decimal(p.getDistance()),
                        p.getLatitude(),
                        p.getLongitude(),

                        p.getLogoUrl()
                ))
                .toList();
    }



    public List<LocalDate> getShowDates(UUID cinemaId) {
        List<java.sql.Date> sqlDates = showtimeRepository.findDistinctShowDates(cinemaId);
        return sqlDates.stream()
                .map(java.sql.Date::toLocalDate)
                .toList();
    }

    public CinemaShowtimeResponse getShowtimes(UUID cinemaId, LocalDate date) {

        List<Showtime> showtimes =
                showtimeRepository.findByCinemaAndDate(cinemaId, date);

        if (showtimes.isEmpty()) {
            return new CinemaShowtimeResponse(
                    null,
                    date,
                    new ArrayList<>()
            );
        }

        Cinema cinema = showtimes.get(0).getRoom().getCinema();

        Map<Movie, List<Showtime>> grouped =
                showtimes.stream().collect(Collectors.groupingBy(Showtime::getMovie));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        List<MovieShowtimeDto> movieDtos = new ArrayList<>();

        for (Map.Entry<Movie, List<Showtime>> entry : grouped.entrySet()) {

            Movie movie = entry.getKey();
            List<Showtime> sts = entry.getValue();

            List<ShowtimeDto> showtimeDtos = sts.stream()
                    .map(s -> ShowtimeDto.builder()
                            .id(s.getId().toString()) // 👈 UUID → String
                            .startTime(s.getStartTime().format(formatter))
                            .build()
                    )
                    .toList();

            List<String> genres = movie.getGenres()
                    .stream()
                    .map(Genre::getName)
                    .distinct()
                    .toList();

            movieDtos.add(new MovieShowtimeDto(
                    movie.getId(),
                    movie.getTitle(),
                    movie.getDurationMinutes(),
                    movie.getAgeRating(),
                    movie.getPosterUrl(),
                    movie.getTrailerUrl(),
                    genres,
                    showtimeDtos
            ));
        }

        CinemaDto cinemaDto = CinemaDto.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .address(cinema.getAddress())
                .latitude(cinema.getLatitude())
                .longitude(cinema.getLongitude())
                .logoUrl(cinema.getLogoUrl())
                .distance(null)
                .build();

        return new CinemaShowtimeResponse(
                cinemaDto,
                date,
                movieDtos
        );
    }
}