package com.example.my_movie_app.controller;

import com.example.my_movie_app.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class DataSeederController {

    private final DataSeederService seederService;

    @PostMapping("/showtimes")
    public String seed() {
        seederService.generateShowtimes(3,5);
        return "OK";
    }

    @PostMapping("/showtimes-comming-soon")
    public String seedCommingSoon() {
        seederService.generateShowtimes(7,10);
        return "OK";
    }

    @PostMapping("/import-movies")
    public String importMovies(@RequestParam("file") MultipartFile file) {
        try {
            seederService.importMoviesFromJson(file.getInputStream());
            return "Import thành công!";
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    @PostMapping("/import-banners")
    public String importBanners(@RequestParam("file") MultipartFile file) {
        try {
            seederService.importBannersFromJson(file.getInputStream());
            return "Import thành công!";
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }
}