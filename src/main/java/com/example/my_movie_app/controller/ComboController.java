package com.example.my_movie_app.controller;


import com.example.my_movie_app.dto.request.ComboRequest;
import com.example.my_movie_app.dto.response.ComboResponse;
import com.example.my_movie_app.entity.Combo;
import com.example.my_movie_app.service.ComboService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/combo")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @GetMapping
    public List<Combo> getAllCombos() {
        return comboService.getAllCombos();
    }

    @GetMapping("/active")
    public List<Combo> getActiveCombos() {
        return comboService.getActiveCombos();
    }

    @GetMapping("/{id}")
    public Combo getComboById(@PathVariable UUID id) {
        return comboService.getComboById(id);
    }

    @PostMapping
    public ComboResponse createCombo(
            @ModelAttribute ComboRequest request,
            @RequestParam MultipartFile image
    ) {
        return comboService.createCombo(request, image);
    }

    @PutMapping("/{id}")
    public ComboResponse updateCombo(@PathVariable UUID id, @RequestBody ComboRequest combo,@RequestParam MultipartFile image) {
        return comboService.updateCombo(id, combo,image);
    }

    @DeleteMapping("/{id}")
    public void deleteCombo(@PathVariable UUID id) {
        comboService.deleteCombo(id);
    }

}