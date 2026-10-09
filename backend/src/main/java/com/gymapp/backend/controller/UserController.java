package com.gymapp.backend.controller;

import com.gymapp.backend.dto.UserRegistrationDTO;
import com.gymapp.backend.dto.UserResponseDTO;
import com.gymapp.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        UserResponseDTO responseDTO = userService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}