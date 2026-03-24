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
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class CinemaDataLoader implements CommandLineRunner {

    private final CinemaRepository cinemaRepository;

//    @Override
//    public void run(String... args) throws Exception {
//
//        if (cinemaRepository.count() > 0) {
//            System.out.println("Cinema data already exists, skip import");
//            return;
//        }
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        InputStream is = getClass()
//                .getResourceAsStream("/data/cinemas_all.json");
//
//        List<CinemaJson> cinemas =
//                mapper.readValue(is, new TypeReference<List<CinemaJson>>() {});
//
//        for (CinemaJson item : cinemas) {
//
//            Cinema cinema = Cinema.builder()
//                    .name(item.getName())
//                    .address(item.getAddress())
//                    .description(item.getDescription())
//                    .latitude(item.getLatitude())
//                    .longitude(item.getLongitude())
//                    .region(item.getRegion() != null ? item.getRegion().getName() : null)
//                    .cineplex(item.getCineplex() != null ? item.getCineplex().getName() : null)
//                    .isActive(true)
//                    .build();
//
//            cinemaRepository.save(cinema);
//        }
//
//        System.out.println("Import cinema data done");
//    }

    @Override
    public void run(String... args) throws Exception {


        if (cinemaRepository.count() > 0) {
            System.out.println("Cinema data already exists, skip import");
            return;
        }


        ObjectMapper mapper = new ObjectMapper();


        InputStream is = getClass()
                .getResourceAsStream("/data/cinemas_all.json");


        List<CinemaJson> cinemaJsonList =
                mapper.readValue(is, new TypeReference<List<CinemaJson>>() {});


        for (CinemaJson item : cinemaJsonList) {


            String logoUrl = null;

            if (item.getCineplex() != null &&
                    item.getCineplex().getLogo() != null &&
                    item.getCineplex().getLogo().getSizes() != null) {

                logoUrl = item.getCineplex()
                        .getLogo()
                        .getSizes()
                        .getSquare();
            }


            Cinema cinema = Cinema.builder()
                    .name(item.getName())
                    .address(item.getAddress())
                    .description(item.getDescription())
                    .latitude(item.getLatitude())
                    .longitude(item.getLongitude())
                    .region(
                            item.getRegion() != null
                                    ? item.getRegion().getName()
                                    : null
                    )
                    .cineplex(
                            item.getCineplex() != null
                                    ? item.getCineplex().getName()
                                    : null
                    )
                    .logoUrl(logoUrl)
                    .isActive(true)
                    .build();


            cinemaRepository.save(cinema);
        }


        System.out.println("Import cinema data done!");
    }

//    @Override
//    public void run(String... args) throws Exception {
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        InputStream is = getClass()
//                .getResourceAsStream("/data/cinemas_all.json");
//
//        List<CinemaJson> cinemas =
//                mapper.readValue(is, new TypeReference<List<CinemaJson>>() {});
//
//        for (CinemaJson item : cinemas) {
//
//            List<Cinema> cinemasInDb =
//                    cinemaRepository.findByLatitudeAndLongitude(
//                            item.getLatitude(),
//                            item.getLongitude()
//                    );
//
//            for (Cinema cinema : cinemasInDb) {
//
//                String logoUrl = Optional.ofNullable(item.getCineplex())
//                        .map(CinemaJson.Cineplex::getLogo)
//                        .map(CinemaJson.Logo::getSizes)
//                        .map(CinemaJson.Sizes::getSquare)
//                        .orElse(null);
//
//                cinema.setLogoUrl(logoUrl);
//
//                cinemaRepository.save(cinema);
//            }
//        }
//
//        System.out.println("Update logoUrl done");
//    }
}