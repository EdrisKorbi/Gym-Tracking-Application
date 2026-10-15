package com.gymapp.backend.config;

import com.gymapp.backend.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final Role role; // NEW
    private final boolean isActive; // NEW

    // Constructor updated to receive role and active status
    public CustomUserDetails(UUID id, String email, Role role, boolean isActive) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security requires the "ROLE_" prefix to work with hasRole()
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Maps the soft-delete flag strictly to Spring Security's enabled status
        return isActive;
    }
}