package com.gymapp.backend.service;

import com.gymapp.backend.dto.*;
import com.gymapp.backend.entity.RefreshToken;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.exception.AccessDeniedException;
import com.gymapp.backend.exception.InvalidCredentialsException;
import com.gymapp.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    // Only ONE constructor injecting all dependencies
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
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
        response.setRole(savedUser.getRole());
        response.setActive(savedUser.isActive());
        response.setCreatedAt(savedUser.getCreatedAt());

        return response;
    }

    @Transactional
    public AuthResponseDTO loginUser(UserLoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Security Check: Ensure banned users cannot log in and get new tokens
        if (!user.isActive()) {
            throw new AccessDeniedException("Account is deactivated or banned");
        }

        // Wipe any old refresh tokens before creating a new one (prevents token bloat)
        refreshTokenService.deleteByUserId(user.getId());

        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponseDTO(jwt, refreshToken.getToken());
    }

    @Transactional
    public AuthResponseDTO refreshToken(TokenRefreshRequestDTO request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        // Final Security Check: If they were banned AFTER getting the refresh token, block them here
        if (!user.isActive()) {
            refreshTokenService.deleteByUserId(user.getId());
            throw new AccessDeniedException("Account is deactivated or banned");
        }

        String newJwt = jwtService.generateToken(user);
        return new AuthResponseDTO(newJwt, requestRefreshToken);
    }
}