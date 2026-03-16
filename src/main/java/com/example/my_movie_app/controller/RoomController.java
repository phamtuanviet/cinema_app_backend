package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.request.CreateRoomRequest;
import com.example.my_movie_app.entity.Room;
import com.example.my_movie_app.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public Room getRoomById(@PathVariable UUID id) {
        return roomService.getRoomById(id);
    }

    @GetMapping("/cinema/{cinemaId}")
    public List<Room> getRoomsByCinema(@PathVariable UUID cinemaId) {
        return roomService.getRoomsByCinema(cinemaId);
    }

    @PostMapping
    public Room createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable UUID id, @RequestBody Room room) {
        return roomService.updateRoom(id, room);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable UUID id) {
        roomService.deleteRoom(id);
    }
}