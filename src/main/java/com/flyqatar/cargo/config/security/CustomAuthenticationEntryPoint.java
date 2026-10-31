package com.flyqatar.cargo.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Handles unauthenticated HTTP requests.
 *
 * Examples:
 * - Missing access token.
 * - Expired JWT.
 * - Invalid JWT signature.
 * - Invalid issuer or audience.
 *
 * Returns a consistent JSON response without exposing
 * internal authentication details.
 */
@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        SecurityErrorResponse errorResponse =
                new SecurityErrorResponse(
                        Instant.now(),
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized",
                        "AUTHENTICATION_REQUIRED",
                        "Authentication is required to access this resource",
                        request.getRequestURI()
                );

        // Prevent an existing response body from interfering.
        response.resetBuffer();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Advertise the authentication scheme expected by this API.
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}