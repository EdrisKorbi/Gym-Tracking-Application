package com.gymapp.backend.dto;

public class AuthResponseDTO {
    private String token; // The short-lived JWT
    private String refreshToken; // The long-lived refresh token

    public AuthResponseDTO(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}