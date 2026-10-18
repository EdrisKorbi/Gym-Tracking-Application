package com.gymapp.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.enums.Role;
import com.gymapp.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(
        classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class FullWorkflowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testCompleteEnterpriseHttpWorkflow() throws Exception {
        String email = "e2e.pipeline@gym.com";
        String password = "SecurePass123!";

        // 1. VALIDATION GATE: Invalid email format must return 400 Bad Request
        String invalidRegisterJson = """
                {
                    "firstName": "Test",
                    "lastName": "User",
                    "email": "not-an-email",
                    "password": "%s"
                }
                """.formatted(password);

        HttpResponse<String> badRegResponse = sendPost("/api/v1/users/register", invalidRegisterJson, null);
        assertEquals(400, badRegResponse.statusCode(), "Invalid email should be blocked with 400");

        // 2. REGISTRATION: Valid user registration
        String validRegisterJson = """
                {
                    "firstName": "Edris",
                    "lastName": "Pipeline",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        HttpResponse<String> regResponse = sendPost("/api/v1/users/register", validRegisterJson, null);
        assertTrue(regResponse.statusCode() == 201 || regResponse.statusCode() == 200,
                "Registration must succeed with 200/201");

        // 3. LOGIN: Receive 15-min JWT + 7-day Refresh Token
        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        HttpResponse<String> loginResponse = sendPost("/api/v1/users/login", loginJson, null);
        assertEquals(200, loginResponse.statusCode(), "Valid login must return 200 OK");

        JsonNode loginBody = objectMapper.readTree(loginResponse.body());
        String jwtToken = loginBody.path("data").path("token").asText();
        String refreshToken = loginBody.path("data").path("refreshToken").asText();
        assertFalse(jwtToken.isBlank(), "JWT token must be present");
        assertFalse(refreshToken.isBlank(), "Refresh token must be present");

        // 4. SECURITY FILTER: Unauthenticated GET /me must be rejected (401 or 403)
        HttpResponse<String> unauthMeResponse = sendGet("/api/v1/users/me", null);
        assertTrue(unauthMeResponse.statusCode() == 401 || unauthMeResponse.statusCode() == 403,
                "Unauthenticated access to /me must be blocked");

        // 5. BLUEPRINT CREATION (Program)
        String programJson = """
                {
                    "name": "E2E Automated Program",
                    "description": "Integration Test Blueprint",
                    "isTemplate": false,
                    "phases": [
                        {
                            "name": "Foundation Phase",
                            "phaseOrder": 1,
                            "routines": [
                                {
                                    "name": "Full Body Workout",
                                    "dayNumber": 1,
                                    "routineExercises": [
                                        {
                                            "name": "Barbell Squats",
                                            "targetSets": 3,
                                            "targetReps": 10,
                                            "exerciseOrder": 1
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
                """;
        HttpResponse<String> programResponse = sendPost("/api/v1/programs", programJson, jwtToken);
        assertTrue(programResponse.statusCode() == 200 || programResponse.statusCode() == 201,
                "Program creation failed! Backend Response: " + programResponse.body());

        JsonNode programBody = objectMapper.readTree(programResponse.body());
        String programId = programBody.path("data").path("id").asText();

        // 6. WORKOUT GENERATION
        String generateRequestJson = """
                {
                    "startDate": "2026-11-01T00:00:00Z",
                    "preferredDays": ["MONDAY", "WEDNESDAY", "FRIDAY"]
                }
                """;
        HttpResponse<String> generateResponse = sendPost("/api/v1/programs/" + programId + "/generate", generateRequestJson, jwtToken);
        assertTrue(generateResponse.statusCode() == 200 || generateResponse.statusCode() == 201,
                "Workout generation failed! Backend Response: " + generateResponse.body());

        // 6.5 SECURITY CHECK: Standard user cannot update their personal program to a Master Template
        String maliciousUpdateJson = """
                {
                    "name": "Hacked Master Template",
                    "description": "Trying to bypass security",
                    "isTemplate": true,
                    "phases": [
                        {
                            "name": "Foundation Phase",
                            "phaseOrder": 1,
                            "routines": [
                                {
                                    "name": "Full Body Workout",
                                    "dayNumber": 1,
                                    "routineExercises": [
                                        {
                                            "name": "Barbell Squats",
                                            "targetSets": 3,
                                            "targetReps": 10,
                                            "exerciseOrder": 1
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
                """;

        HttpResponse<String> maliciousUpdateResponse = sendPut("/api/v1/programs/" + programId, maliciousUpdateJson, jwtToken);
        assertEquals(403, maliciousUpdateResponse.statusCode(),
                "Standard user must be blocked from publishing Master Templates via UPDATE! Backend Response: " + maliciousUpdateResponse.body());

        // 7. SELF-SERVICE /me: Authenticated GET /me must return principal profile
        HttpResponse<String> authMeResponse = sendGet("/api/v1/users/me", jwtToken);
        assertEquals(200, authMeResponse.statusCode(), "Authenticated /me must return 200 OK");
        JsonNode meBody = objectMapper.readTree(authMeResponse.body());
        assertEquals(email, meBody.path("data").path("email").asText());

        // 7.5 RBAC NEGATIVE CHECK: Standard USER must be blocked from Admin endpoints
        HttpResponse<String> userAdminAttempt = sendGet("/api/v1/admin/users", jwtToken);
        assertEquals(403, userAdminAttempt.statusCode(),
                "Standard USER must receive 403 Forbidden on /api/v1/admin/users! Backend Response: " + userAdminAttempt.body());

        // 8. PAGINATED WORKOUTS: Fetch default paginated workouts for user
        HttpResponse<String> workoutsResponse = sendGet("/api/v1/workouts", jwtToken);
        assertEquals(200, workoutsResponse.statusCode(), "GET /api/v1/workouts must return 200 OK");

        // 9. REFRESH TOKEN FLOW: Exchange refreshToken for a brand-new JWT
        String refreshRequestJson = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        HttpResponse<String> refreshResponse = sendPost("/api/v1/users/refresh", refreshRequestJson, null);
        assertEquals(200, refreshResponse.statusCode(), "Token refresh must return 200 OK");
        String refreshedJwt = objectMapper.readTree(refreshResponse.body()).path("data").path("token").asText();
        assertFalse(refreshedJwt.isBlank(), "Refreshed JWT must not be blank");

        // 10. SOFT DELETE: Deactivate account via DELETE /api/v1/users/me
        HttpResponse<String> deleteResponse = sendDelete("/api/v1/users/me", refreshedJwt);
        assertTrue(deleteResponse.statusCode() == 200 || deleteResponse.statusCode() == 204,
                "Soft delete must succeed");

        // 11. REVOCATION ENFORCEMENT: Refreshing token after soft delete must be blocked
        HttpResponse<String> blockedRefreshResponse = sendPost("/api/v1/users/refresh", refreshRequestJson, null);
        assertTrue(blockedRefreshResponse.statusCode() == 401 || blockedRefreshResponse.statusCode() == 403,
                "Refresh token for soft-deleted user must be rejected! Backend Response: " + blockedRefreshResponse.body());

        // 12. LOGIN BLOCK: Logging in after soft delete must be rejected
        HttpResponse<String> blockedLoginResponse = sendPost("/api/v1/users/login", loginJson, null);
        assertTrue(blockedLoginResponse.statusCode() == 401 || blockedLoginResponse.statusCode() == 403,
                "Login for soft-deleted user must be rejected! Backend Response: " + blockedLoginResponse.body());

        // 13. ADMIN POSITIVE PATH: Promote user, login, and access back-office
        String adminEmail = "admin.pipeline@gym.com";
        String adminRegJson = """
                {
                    "firstName": "Super",
                    "lastName": "Admin",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(adminEmail, password);
        sendPost("/api/v1/users/register", adminRegJson, null);

        // Simulate direct DB access to grant the first Admin role
        User adminUser = userRepository.findByEmail(adminEmail).orElseThrow();
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        // Login to get the Admin JWT
        String adminLoginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(adminEmail, password);
        HttpResponse<String> adminLoginResponse = sendPost("/api/v1/users/login", adminLoginJson, null);
        String adminJwt = objectMapper.readTree(adminLoginResponse.body()).path("data").path("token").asText();

        // Test GET /admin/users (Should return 200 OK and a paginated list)
        HttpResponse<String> adminGetUsers = sendGet("/api/v1/admin/users", adminJwt);
        assertEquals(200, adminGetUsers.statusCode(), "Admin must be able to view user list");

        // Test PATCH /admin/users/{id}/status (Admin banning the pipeline user)
        String targetUserId = meBody.path("data").path("id").asText();
        String banJson = """
                {
                    "isActive": false
                }
                """;
        HttpResponse<String> banResponse = sendPatch("/api/v1/admin/users/" + targetUserId + "/status", banJson, adminJwt);
        assertEquals(200, banResponse.statusCode(), "Admin must be able to change user status");

        // Test PATCH /admin/users/{id}/role (Admin promoting the user to ADMIN)
        String roleJson = """
                {
                    "role": "ADMIN"
                }
                """;
        HttpResponse<String> roleResponse = sendPatch("/api/v1/admin/users/" + targetUserId + "/role", roleJson, adminJwt);
        assertEquals(200, roleResponse.statusCode(),
                "Admin must be able to change user role! Backend Response: " + roleResponse.body());

        JsonNode roleResponseBody = objectMapper.readTree(roleResponse.body());
        assertEquals("ADMIN", roleResponseBody.path("data").path("role").asText(),
                "Returned payload must confirm the user role is now ADMIN");

        // 14. ADMIN SELF-LOCKOUT PROTECTION: Admin cannot deactivate or demote themselves
        String adminId = adminUser.getId().toString();

        HttpResponse<String> selfBanAttempt = sendPatch("/api/v1/admin/users/" + adminId + "/status", banJson, adminJwt);
        assertEquals(403, selfBanAttempt.statusCode(),
                "Admin must be prevented from deactivating their own account! Backend Response: " + selfBanAttempt.body());

        String selfDemoteJson = """
                {
                    "role": "USER"
                }
                """;
        HttpResponse<String> selfDemoteAttempt = sendPatch("/api/v1/admin/users/" + adminId + "/role", selfDemoteJson, adminJwt);
        assertEquals(403, selfDemoteAttempt.statusCode(),
                "Admin must be prevented from modifying their own role! Backend Response: " + selfDemoteAttempt.body());
    }

    private HttpResponse<String> sendPost(String path, String jsonBody, String bearerToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGet(String path, String bearerToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .GET();
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path, String bearerToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .DELETE();
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPatch(String path, String jsonBody, String bearerToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody));
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPut(String path, String jsonBody, String bearerToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}