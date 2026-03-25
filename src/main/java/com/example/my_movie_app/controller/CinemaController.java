package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.CinemaDto;
import com.example.my_movie_app.dto.RegionDto;
import com.example.my_movie_app.dto.response.CinemaShowtimeResponse;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.service.CinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cinema")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;


    @GetMapping
    public List<Cinema> getAllCinemas() {
        return cinemaService.getAllCinemas();
    }

    @GetMapping("/active")
    public List<Cinema> getActiveCinemas() {
        return cinemaService.getActiveCinemas();
    }

    @GetMapping("/{id}")
    public Cinema getCinemaById(@PathVariable UUID id) {
        return cinemaService.getCinemaById(id);
    }

    @GetMapping("/region/{region}")
    public List<Cinema> getByRegion(@PathVariable String region) {
        return cinemaService.getCinemasByRegion(region);
    }

    @PostMapping
    public Cinema createCinema(@RequestBody Cinema cinema) {
        return cinemaService.createCinema(cinema);
    }

    @PutMapping("/{id}")
    public Cinema updateCinema(@PathVariable UUID id, @RequestBody Cinema cinema) {
        return cinemaService.updateCinema(id, cinema);
    }

    @DeleteMapping("/{id}")
    public void deleteCinema(@PathVariable UUID id) {
        cinemaService.deleteCinema(id);
    }

    @GetMapping("/nearby")
    public List<CinemaDto> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "100") double radius
    ) {
        return cinemaService.getNearby(lat, lng, radius);
    }

    // 2. Regions
    @GetMapping("/regions")
    public List<RegionDto> getRegions() {
        return cinemaService.getRegions();
    }

    // 3. Cinema theo region + distance
    @GetMapping("/by-region")
    public List<CinemaDto> getByRegion(
            @RequestParam String region,
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return cinemaService.getByRegion(region, lat, lng);
    }

    @GetMapping("/{cinemaId}/showtime-dates")
    public ResponseEntity<List<LocalDate>> getShowDates(
            @PathVariable UUID cinemaId
    ) {
        return ResponseEntity.ok(
                cinemaService.getShowDates(cinemaId)
        );
    }

    @GetMapping("/{cinemaId}/showtimes")
    public ResponseEntity<CinemaShowtimeResponse> getShowtimes(
            @PathVariable UUID cinemaId,
            @RequestParam String date
    ) {
        return ResponseEntity.ok(
                cinemaService.getShowtimes(
                        cinemaId,
                        LocalDate.parse(date)
                )
        );
    }
}