package com.gymapp.backend.controller;

import com.gymapp.backend.config.CustomUserDetails;
import com.gymapp.backend.dto.ApiResponse;
import com.gymapp.backend.dto.UserResponseDTO;
import com.gymapp.backend.dto.UserRoleUpdateDTO;
import com.gymapp.backend.dto.UserStatusUpdateDTO;
import com.gymapp.backend.exception.AccessDeniedException;
import com.gymapp.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.SortDefault;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails currentAdmin,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "role", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "isActive", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            }) Pageable pageable) {
        Page<UserResponseDTO> usersPage = userService.getAllUsers(search, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", usersPage)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateDTO statusDTO,
            @AuthenticationPrincipal CustomUserDetails currentAdmin) {

        if (currentAdmin.getId().equals(id)) {
            throw new AccessDeniedException("Admins cannot deactivate their own account");
        }

        UserResponseDTO updatedUser = userService.updateUserStatus(id, statusDTO.getIsActive());
        return ResponseEntity.ok(
                ApiResponse.success("User status updated successfully", updatedUser)
        );
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UserRoleUpdateDTO roleDTO,
            @AuthenticationPrincipal CustomUserDetails currentAdmin) {

        if (currentAdmin.getId().equals(id)) {
            throw new AccessDeniedException("Admins cannot modify their own role");
        }

        UserResponseDTO updatedUser = userService.updateUserRole(id, roleDTO.getRole());
        return ResponseEntity.ok(
                ApiResponse.success("User role updated successfully", updatedUser)
        );
    }
}