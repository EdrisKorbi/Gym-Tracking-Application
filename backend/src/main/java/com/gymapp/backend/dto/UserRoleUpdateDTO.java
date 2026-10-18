package com.gymapp.backend.dto;

import com.gymapp.backend.enums.Role;
import jakarta.validation.constraints.NotNull;

public class UserRoleUpdateDTO {

    @NotNull(message = "Role cannot be null")
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}