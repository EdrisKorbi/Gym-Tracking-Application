package com.gymapp.backend.service;

import com.gymapp.backend.dto.AuthResponseDTO;
import com.gymapp.backend.dto.UserLoginDTO;
import com.gymapp.backend.dto.UserRegistrationDTO;
import com.gymapp.backend.dto.UserResponseDTO;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.exception.InvalidCredentialsException;
import com.gymapp.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        // role and isActive are automatically set by the User entity defaults (USER and true)

        User savedUser = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setRole(savedUser.getRole()); // NEW: Map role to DTO
        response.setActive(savedUser.isActive()); // NEW: Map status to DTO
        response.setCreatedAt(savedUser.getCreatedAt());

        return response;
    }

    public AuthResponseDTO loginUser(UserLoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // NEW: Check if the account has been soft-deleted by an admin or the user themselves
        if (!user.isActive()) {
            throw new InvalidCredentialsException("This account has been deactivated.");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token);
    }
}