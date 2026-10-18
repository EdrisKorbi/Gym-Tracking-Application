package com.gymapp.backend.service;

import com.gymapp.backend.dto.UserResponseDTO;
import com.gymapp.backend.dto.UserUpdateDTO;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.exception.InvalidCredentialsException;
import com.gymapp.backend.exception.ResourceNotFoundException;
import com.gymapp.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // NEW: Re-injected for credential hashing

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUserProfile(UUID userId, UserUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    // NEW: Secure password change logic
    @Transactional
    public void updatePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify the old password matches what is in the database
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Incorrect current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(false);
        userRepository.save(user);
    }

    // --- ADMIN METHODS ---

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        // .map() automatically converts the Page<User> from the DB into a Page<UserResponseDTO>
        return userRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional
    public UserResponseDTO updateUserStatus(UUID targetUserId, boolean isActive) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);

        return mapToDTO(updatedUser);
    }

    @Transactional
    public UserResponseDTO updateUserRole(UUID targetUserId, com.gymapp.backend.enums.Role newRole) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        return mapToDTO(updatedUser);
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}