package com.example.my_movie_app.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateRoomRequest {

    private UUID cinemaId;

    private String name;

    private Integer rows;

    private Integer seatsPerRow;

    private List<Integer> vipRows;

    private List<Integer> coupleRows;
}