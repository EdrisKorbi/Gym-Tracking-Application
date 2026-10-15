package com.gymapp.backend.controller;

import com.gymapp.backend.config.CustomUserDetails;
import com.gymapp.backend.dto.*;
import com.gymapp.backend.service.AuthService;
import com.gymapp.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        UserResponseDTO responseDTO = authService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("User registered successfully", responseDTO)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        AuthResponseDTO token = authService.loginUser(loginDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", token)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UserResponseDTO profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", profile)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserUpdateDTO updateDTO) {

        UserResponseDTO updatedProfile = userService.updateUserProfile(currentUser.getId(), updateDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updatedProfile)
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody PasswordUpdateDTO passwordDTO) {

        userService.updatePassword(currentUser.getId(), passwordDTO.getCurrentPassword(), passwordDTO.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        userService.deactivateUser(currentUser.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Account deactivated successfully")
        );
    }
}