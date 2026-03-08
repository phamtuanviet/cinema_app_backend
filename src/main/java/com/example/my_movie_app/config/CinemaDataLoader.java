package com.example.my_movie_app.config;


import com.example.my_movie_app.dto.json.CinemaJson;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.repository.CinemaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CinemaDataLoader implements CommandLineRunner {

    private final CinemaRepository cinemaRepository;

    @Override
    public void run(String... args) throws Exception {

        if (cinemaRepository.count() > 0) {
            System.out.println("Cinema data already exists, skip import");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        InputStream is = getClass()
                .getResourceAsStream("/data/cinemas_all.json");

        List<CinemaJson> cinemas =
                mapper.readValue(is, new TypeReference<List<CinemaJson>>() {});

        for (CinemaJson item : cinemas) {

            Cinema cinema = Cinema.builder()
                    .name(item.getName())
                    .address(item.getAddress())
                    .description(item.getDescription())
                    .latitude(item.getLatitude())
                    .longitude(item.getLongitude())
                    .region(item.getRegion() != null ? item.getRegion().getName() : null)
                    .cineplex(item.getCineplex() != null ? item.getCineplex().getName() : null)
                    .isActive(true)
                    .build();

            cinemaRepository.save(cinema);
        }

        System.out.println("Import cinema data done");
    }
}