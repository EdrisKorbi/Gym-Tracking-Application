package com.gymapp.backend;

import com.gymapp.backend.dto.UserLoginDTO;
import com.gymapp.backend.dto.UserRegistrationDTO;
import com.gymapp.backend.dto.UserResponseDTO;
import com.gymapp.backend.enums.Role;
import com.gymapp.backend.exception.InvalidCredentialsException;
import com.gymapp.backend.service.AuthService;
import com.gymapp.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IamIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Test
    void testUserRegistrationAndSoftDeleteLifecycle() {
        // 1. Register a new user
        UserRegistrationDTO regDTO = new UserRegistrationDTO();
        regDTO.setEmail("test.softdelete@gym.com");
        regDTO.setPassword("SecurePass123!");
        regDTO.setFirstName("John");
        regDTO.setLastName("Doe");

        UserResponseDTO savedUser = authService.registerUser(regDTO);

        // 2. Verify Flyway Database Defaults applied correctly
        assertNotNull(savedUser.getId(), "User ID should be generated");
        assertEquals(Role.USER, savedUser.getRole(), "Default role must be USER");
        assertTrue(savedUser.isActive(), "New users must be active by default");

        // 3. Deactivate the user (Soft Delete)
        userService.deactivateUser(savedUser.getId());

        // 4. Attempt to log in with the deactivated account
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("test.softdelete@gym.com");
        loginDTO.setPassword("SecurePass123!");

        // 5. Assert that the correct exception is thrown to block the login
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUser(loginDTO)
        );

        // 6. Verify the exact security message
        assertEquals("This account has been deactivated.", exception.getMessage());
    }
}