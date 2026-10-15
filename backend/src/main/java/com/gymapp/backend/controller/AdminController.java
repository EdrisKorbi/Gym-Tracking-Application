package com.gymapp.backend.controller;

import com.gymapp.backend.dto.ApiResponse;
import com.gymapp.backend.dto.UserResponseDTO;
import com.gymapp.backend.dto.UserStatusUpdateDTO;
import com.gymapp.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')") // Secures EVERY endpoint in this controller
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(Pageable pageable) {
        Page<UserResponseDTO> usersPage = userService.getAllUsers(pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", usersPage)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateDTO statusDTO) {

        UserResponseDTO updatedUser = userService.updateUserStatus(id, statusDTO.getIsActive());
        return ResponseEntity.ok(
                ApiResponse.success("User status updated successfully", updatedUser)
        );
    }
}