package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.request.ComboRequest;
import com.example.my_movie_app.dto.response.ComboResponse;
import com.example.my_movie_app.entity.Combo;
import com.example.my_movie_app.repository.ComboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ComboService {

    private final ComboRepository comboRepository;
    private final CloudinaryService cloudinaryService;


    public List<Combo> getAllCombos() {
        return comboRepository.findAll();
    }

    public List<Combo> getActiveCombos() {
        return comboRepository.findByIsActiveTrue();
    }

    public Combo getComboById(UUID id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Combo not found"));
    }

    public ComboResponse createCombo(ComboRequest request, MultipartFile image) {

        String imageUrl = cloudinaryService.uploadImage(image);

        Combo combo = new Combo();
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setPrice(request.getPrice());
        combo.setImageUrl(imageUrl);
        combo.setIsActive(true);

        comboRepository.save(combo);

        return ComboResponse.builder()
                .id(combo.getId())
                .name(combo.getName())
                .description(combo.getDescription())
                .price(combo.getPrice())
                .imageUrl(combo.getImageUrl())
                .build();
    }

    public ComboResponse updateCombo(UUID id, ComboRequest request, MultipartFile image) {

        Combo existing = comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Combo not found"));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            existing.setImageUrl(imageUrl);
        }

        comboRepository.save(existing);

        return ComboResponse.builder()
                .id(existing.getId())
                .name(existing.getName())
                .description(existing.getDescription())
                .price(existing.getPrice())
                .imageUrl(existing.getImageUrl())
                .build();
    }

    public void deleteCombo(UUID id) {
        comboRepository.deleteById(id);
    }

}