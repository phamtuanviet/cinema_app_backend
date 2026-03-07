package com.example.my_movie_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MyMovieAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyMovieAppApplication.class, args);
    }

}
