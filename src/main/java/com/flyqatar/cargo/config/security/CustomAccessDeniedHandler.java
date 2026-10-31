package com.flyqatar.cargo.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Handles authenticated users who do not have the
 * required permission for a requested operation.
 *
 * Example:
 * cargo-reader attempts to create a shipment without
 * having the SHIPMENT_CREATE role.
 */
@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        SecurityErrorResponse errorResponse =
                new SecurityErrorResponse(
                        Instant.now(),
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden",
                        "ACCESS_DENIED",
                        "You do not have permission to perform this operation",
                        request.getRequestURI()
                );

        response.resetBuffer();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}