package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.CinemaDto;
import com.example.my_movie_app.dto.RegionDto;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;

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
}