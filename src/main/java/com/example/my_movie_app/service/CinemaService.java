package com.example.my_movie_app.service;

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
}