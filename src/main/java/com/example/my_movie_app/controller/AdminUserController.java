package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.dto.AdminUserDetailDto;
import com.example.my_movie_app.dto.AdminUserDto;
import com.example.my_movie_app.dto.AdminUserUpdateRequest;
import com.example.my_movie_app.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public AdminPaginatedResponse<AdminUserDto> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "USER") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminUserService.getUsers(search, role, page, size);
    }

    @PutMapping("/{id}")
    public AdminUserDto updateUser(
            @PathVariable UUID id,
            @RequestBody AdminUserUpdateRequest request
    ) {
        return adminUserService.updateUser(id, request);
    }

    @GetMapping("/{id}/details")
    public AdminUserDetailDto getUserDetail(@PathVariable UUID id) {
        return adminUserService.getUserDetail(id);
    }
}