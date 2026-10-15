package com.gymapp.backend.dto;

import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateDTO {

    @NotNull(message = "isActive status is required")
    private Boolean isActive;

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}