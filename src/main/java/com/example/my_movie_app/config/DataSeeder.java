package com.example.my_movie_app.config;

import com.example.my_movie_app.dto.request.CreateRoomRequest;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.repository.CinemaRepository;
import com.example.my_movie_app.repository.RoomRepository;
import com.example.my_movie_app.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CinemaRepository cinemaRepository;
    private final RoomService roomService;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) {

        // nếu đã có room thì không seed nữa
        if (roomRepository.count() > 0) {
            System.out.println("Rooms already exist. Skipping seeding...");
            return;
        }

        List<Cinema> cinemas = cinemaRepository.findAll();

        for (Cinema cinema : cinemas) {

            for (int i = 1; i <= 6; i++) {

                CreateRoomRequest request = new CreateRoomRequest();

                request.setCinemaId(cinema.getId());
                request.setName("Room " + i);
                request.setRows(11);
                request.setSeatsPerRow(10);

                // VIP rows
                request.setVipRows(List.of(6,7,8,9,10));

                // couple row
                request.setCoupleRows(List.of(11));

                roomService.createRoom(request);
            }
        }

        System.out.println("Seeded rooms and seats successfully!");
    }
}