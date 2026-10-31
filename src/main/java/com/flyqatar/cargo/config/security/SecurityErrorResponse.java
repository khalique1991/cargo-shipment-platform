package com.flyqatar.cargo.config.security;

import java.time.Instant;

/**
 * Standard API response returned when Spring Security
 * rejects an authentication or authorization request.
 *
 * Keeping a consistent response structure makes it easier
 * for API consumers, frontend applications and monitoring
 * systems to process security failures.
 */
public record SecurityErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
}