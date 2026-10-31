package com.flyqatar.cargo.adapter.in.rest.exception;

import com.flyqatar.cargo.adapter.out.integration.security
        .OAuth2TokenAcquisitionException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class IntegrationExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(IntegrationExceptionHandler.class);

    @ExceptionHandler(OAuth2TokenAcquisitionException.class)
    public ResponseEntity<IntegrationErrorResponse> handleTokenFailure(
            OAuth2TokenAcquisitionException exception,
            HttpServletRequest request) {

        // Log the failure internally without exposing token details.
        log.error("Outbound OAuth2 token acquisition failed. Path: {}",
                request.getRequestURI());

        IntegrationErrorResponse response =
                new IntegrationErrorResponse(
                        Instant.now(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Service Unavailable",
                        "OAUTH2_TOKEN_ACQUISITION_FAILED",
                        "The downstream service authentication is temporarily unavailable",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}