package com.gymapp.backend.config;

import com.gymapp.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Check if the request has an Authorization header with a Bearer token
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the token
        final String jwt = authHeader.substring(7);

        // 3. Validate the token
        if (jwtService.isTokenValid(jwt)) {
            String email = jwtService.extractEmail(jwt);
            String userIdString = jwtService.extractUserId(jwt); // NEW: Extract the UUID

            // 4. Create our Custom Profile Card if not already authenticated
            if (email != null && userIdString != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Convert the String from the JWT back into a real Java UUID
                UUID userId = UUID.fromString(userIdString);

                // Create our custom profile
                CustomUserDetails userDetails = new CustomUserDetails(userId, email);

                // Pass the ENTIRE userDetails object into Spring Security, not just the email
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 5. Continue processing the request
        filterChain.doFilter(request, response);
    }
}