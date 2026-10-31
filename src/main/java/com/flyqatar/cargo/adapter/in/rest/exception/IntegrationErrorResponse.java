package com.flyqatar.cargo.adapter.in.rest.exception;

import java.time.Instant;

public record IntegrationErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {}