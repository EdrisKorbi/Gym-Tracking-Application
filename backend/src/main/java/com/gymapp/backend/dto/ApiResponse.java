package com.gymapp.backend.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private LocalDateTime timestamp;
    private boolean success;
    private String message;
    private T data;

    // Private constructor to force use of the static methods below
    private ApiResponse(boolean success, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 1. Success with Data and Message
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 2. Success with Data only (Default message)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Request processed successfully", data);
    }

    // 3. Success with Message only (No data payload)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}