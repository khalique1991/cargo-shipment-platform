
package com.flyqatar.cargo.config.resilience;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flyqatar.cargo.adapter.out.integration.security
        .OAuth2TokenAcquisitionException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Configuration
public class KeycloakCircuitBreakerConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KeycloakCircuitBreakerConfig.class);

    @Bean
    public CircuitBreaker keycloakTokenCircuitBreaker() {

        CircuitBreakerConfig config =
                CircuitBreakerConfig.custom()

                        // Evaluate the last five completed calls.
                        .slidingWindowSize(5)

                        // Require at least three calls before evaluating
                        // the failure rate.
                        .minimumNumberOfCalls(3)

                        // Open the circuit when 50% or more calls fail.
                        .failureRateThreshold(50)

                        // Keep the circuit open for 10 seconds.
                        .waitDurationInOpenState(Duration.ofSeconds(10))

                        // Permit two test calls in HALF_OPEN state.
                        .permittedNumberOfCallsInHalfOpenState(2)

                        // Only count outbound OAuth2 acquisition failures.
                        .recordException(exception ->
                                exception instanceof
                                        OAuth2TokenAcquisitionException)

                        .build();

        CircuitBreaker circuitBreaker =
                CircuitBreaker.of("keycloakToken", config);

        // Log circuit state transitions for operational visibility.
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn(
                                "Keycloak circuit breaker state changed: {}",
                                event.getStateTransition()
                        ));

        return circuitBreaker;
    }
}
