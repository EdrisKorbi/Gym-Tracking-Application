package com.gymapp.backend.service;

import com.gymapp.backend.dto.UserRegistrationDTO;
import com.gymapp.backend.dto.UserResponseDTO;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {

        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());

        user.setPasswordHash(registrationDTO.getPassword() + "_mock_hash");

        User savedUser = userRepository.save(user);

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(savedUser.getId());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setFirstName(savedUser.getFirstName());
        responseDTO.setLastName(savedUser.getLastName());
        responseDTO.setCreatedAt(savedUser.getCreatedAt());

        return responseDTO;
    }
}