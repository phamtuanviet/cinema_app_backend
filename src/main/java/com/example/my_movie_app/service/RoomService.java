package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.request.CreateRoomRequest;
import com.example.my_movie_app.entity.Cinema;
import com.example.my_movie_app.entity.Room;
import com.example.my_movie_app.entity.Seat;
import com.example.my_movie_app.enums.SeatType;
import com.example.my_movie_app.repository.CinemaRepository;
import com.example.my_movie_app.repository.RoomRepository;
import com.example.my_movie_app.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {


    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final CinemaRepository cinemaRepository;

    private BigDecimal getPriceModifier(SeatType seatType) {
        switch (seatType) {
            case VIP:
                return new BigDecimal("10000");
            case NORMAL:
            case COUPLE:
            default:
                return BigDecimal.ZERO;
        }
    }


    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByCinema(UUID cinemaId) {
        return roomRepository.findByCinemaId(cinemaId);
    }

    public Room getRoomById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    @Transactional
    public Room createRoom(CreateRoomRequest request) {

        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new RuntimeException("Cinema not found"));

        Room room = Room.builder()
                .cinema(cinema)
                .name(request.getName())
                .totalSeats(request.getRows() * request.getSeatsPerRow())
                .build();

        roomRepository.save(room);

        generateSeats(room, request);

        return room;
    }
    private void generateSeats(Room room, CreateRoomRequest request) {

        List<Seat> seats = new ArrayList<>();

        for (int row = 1; row <= request.getRows(); row++) {

            String seatRow = String.valueOf((char) ('A' + row - 1));

            for (int num = 1; num <= request.getSeatsPerRow(); num++) {

                SeatType type = SeatType.NORMAL;

                if (request.getVipRows() != null && request.getVipRows().contains(row)) {
                    type = SeatType.VIP;
                }

                if (request.getCoupleRows() != null && request.getCoupleRows().contains(row)) {
                    type = SeatType.COUPLE;
                }

                BigDecimal priceModifier = getPriceModifier(type);

                Seat seat = Seat.builder()
                        .room(room)
                        .seatRow(seatRow)
                        .seatNumber(num)
                        .seatType(type)
                        .priceModifier(priceModifier)
                        .build();

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
    }

    public Room updateRoom(UUID id, Room room) {

        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        existing.setName(room.getName());
        existing.setTotalSeats(room.getTotalSeats());
        existing.setCinema(room.getCinema());

        return roomRepository.save(existing);
    }

    public void deleteRoom(UUID id) {
        roomRepository.deleteById(id);
    }
}