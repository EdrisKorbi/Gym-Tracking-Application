package com.gymapp.backend.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;

    // Constructor to build the card
    public CustomUserDetails(UUID id, String email) {
        this.id = id;
        this.email = email;
    }

    // Our custom getter so controllers can extract the exact database ID
    public UUID getId() {
        return id;
    }

    // --- Spring Security Required Methods Below ---

    @Override
    public String getUsername() {
        // Spring calls it "username", but in our app, the email is the username.
        return email;
    }

    @Override
    public String getPassword() {
        // We return null here. Why? Because by the time this object is created,
        // the JWT has already proven they are logged in. We don't need their password anymore.
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // We will return an empty list for now until we add Roles (ADMIN, USER) later.
        return Collections.emptyList();
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
        return true;
    }
}