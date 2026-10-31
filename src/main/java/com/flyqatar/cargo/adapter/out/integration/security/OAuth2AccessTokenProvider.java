
package com.flyqatar.cargo.adapter.out.integration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import java.util.concurrent.ThreadLocalRandom;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class OAuth2AccessTokenProvider {

    private static final Logger log =
            LoggerFactory.getLogger(OAuth2AccessTokenProvider.class);

    private static final String REGISTRATION_ID =
            "cargo-booking-client";

    private static final String SERVICE_PRINCIPAL =
            "cargo-shipment-service";

    private static final int MAX_RETRIES = 3;

    private static final long INITIAL_DELAY_MS = 500;

    private static final long MAX_JITTER_MS = 250;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final CircuitBreaker circuitBreaker;

    public OAuth2AccessTokenProvider(
            OAuth2AuthorizedClientManager authorizedClientManager, CircuitBreaker circuitBreaker) {
        this.authorizedClientManager = authorizedClientManager;
        this.circuitBreaker = circuitBreaker;
    }

    private String acquireAccessTokenWithRetry() {

        OAuth2AuthorizeRequest request =
                OAuth2AuthorizeRequest
                        .withClientRegistrationId(REGISTRATION_ID)
                        .principal(SERVICE_PRINCIPAL)
                        .build();

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {

            try {

                OAuth2AuthorizedClient authorizedClient =
                        authorizedClientManager.authorize(request);

                if (authorizedClient == null ||
                        authorizedClient.getAccessToken() == null) {

                    throw new OAuth2TokenAcquisitionException(
                            "Unable to obtain OAuth2 access token", null);
                }

                log.info("OAuth2 service-account token acquired successfully");

                return authorizedClient.getAccessToken().getTokenValue();

            } catch (RuntimeException exception) {

                boolean retryable = isTransientFailure(exception);

                if (!retryable || attempt == MAX_RETRIES) {

                    log.error(
                            "OAuth2 token acquisition failed. Attempt: {}",
                            attempt + 1);

                    throw new OAuth2TokenAcquisitionException(
                            "OAuth2 service authentication failed",
                            exception);
                }

                long delay = calculateBackoff(attempt);

                log.warn(
                        "Temporary OAuth2 token acquisition failure. " +
                                "Retry attempt: {} of {}. Waiting {} ms.",
                        attempt + 1,
                        MAX_RETRIES,
                        delay);

                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException interruptedException) {

                    Thread.currentThread().interrupt();

                    throw new OAuth2TokenAcquisitionException(
                            "OAuth2 token retry interrupted",
                            interruptedException);
                }
            }
        }

        throw new OAuth2TokenAcquisitionException(
                "OAuth2 token acquisition exhausted", null);
    }

    private boolean isTransientFailure(Throwable exception) {

        Throwable current = exception;

        while (current != null) {

            // Network connection failures and timeouts.
            if (current instanceof ResourceAccessException) {
                return true;
            }

            // Temporary HTTP server failures.
            if (current instanceof HttpServerErrorException) {
                return true;
            }

            // Underlying Java network failures.
            if (current instanceof java.net.ConnectException ||
                    current instanceof java.net.SocketTimeoutException) {
                return true;
            }

            // Explicit temporary OAuth2 errors.
            if (current instanceof OAuth2AuthorizationException oauthException) {

                OAuth2Error error = oauthException.getError();

                if (error != null) {
                    String code = error.getErrorCode();

                    if ("server_error".equals(code) ||
                            "temporarily_unavailable".equals(code)) {
                        return true;
                    }
                }
            }

            current = current.getCause();
        }

        return false;
    }

    private long calculateBackoff(int attempt) {

        long baseDelay = INITIAL_DELAY_MS * (1L << attempt);

        long jitter = ThreadLocalRandom.current()
                .nextLong(MAX_JITTER_MS + 1);

        return baseDelay + jitter;
    }

    public String getAccessToken() {

        try {

            // Circuit Breaker wraps the complete retry operation.
            // It records one final success or failure per invocation.
            return circuitBreaker.executeSupplier(
                    this::acquireAccessTokenWithRetry
            );

        } catch (CallNotPermittedException exception) {

            // Circuit is OPEN. Do not attempt another Keycloak call.
            log.warn(
                    "Keycloak circuit breaker is OPEN. " +
                            "Token acquisition rejected."
            );

            throw new OAuth2TokenAcquisitionException(
                    "Keycloak authentication is temporarily unavailable",
                    exception
            );
        }
    }

}
