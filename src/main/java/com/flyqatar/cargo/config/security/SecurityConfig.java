
package com.flyqatar.cargo.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import java.time.Duration;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ISSUER_URI =
            "http://localhost:8081/realms/cargo-platform";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource)
                )

                // This is a stateless REST API using Bearer tokens.
                // We do not use browser session-based authentication.
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Defines which Keycloak roles are required for each API.
                .authorizeHttpRequests(auth -> auth

                        // Swagger documentation is publicly accessible.
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Read shipment details and shipment lists.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/shipments",
                                "/api/v1/shipments/**"
                        ).hasRole("SHIPMENT_READ")

                        // Create a shipment.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/shipments"
                        ).hasRole("SHIPMENT_CREATE")

                        // Update a shipment.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/shipments/**"
                        ).hasRole("SHIPMENT_UPDATE")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/shipments/**"
                        ).hasRole("SHIPMENT_UPDATE")

                        // Delete a shipment.
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/shipments/**"
                        ).hasRole("SHIPMENT_ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/integrations/booking/health"
                        ).hasRole("SHIPMENT_READ")
                        .anyRequest().authenticated()
                )

                // Provides consistent JSON responses for security failures.
                .exceptionHandling(exception -> exception

                        // 401: Authentication is missing or invalid.
                        .authenticationEntryPoint(authenticationEntryPoint)

                        // 403: Authenticated user lacks the required permission.
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Enables JWT authentication using Keycloak.
                .oauth2ResourceServer(oauth2 -> oauth2

                        // Configure JWT authentication and Keycloak role conversion.
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )

                        // Handle invalid, expired and malformed Bearer tokens.
                        .authenticationEntryPoint(authenticationEntryPoint)
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                new KeycloakRealmRoleConverter()
        );

        return converter;
    }


    @Bean
    public JwtDecoder jwtDecoder() {

        /*
         * Creates the decoder using Keycloak's issuer metadata.
         *
         * The decoder discovers the JWKS endpoint and obtains
         * public signing keys to verify JWT signatures.
         *
         * Keycloak signing keys can rotate. Using the JWKS endpoint
         * allows the decoder to obtain updated keys instead of
         * hardcoding a public key into our application.
         */
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withIssuerLocation(ISSUER_URI)
                .build();

        /*
         * 1. ISSUER VALIDATION
         *
         * Ensures that the token was issued by our trusted
         * cargo-platform Keycloak realm.
         */
        OAuth2TokenValidator<Jwt> issuerValidator =
                new JwtIssuerValidator(ISSUER_URI);

        /*
         * 2. TIMESTAMP VALIDATION
         *
         * Validates exp and nbf when present.
         *
         * A 60-second clock-skew allowance accommodates small
         * clock differences between distributed application
         * instances and the identity provider.
         */
        OAuth2TokenValidator<Jwt> timestampValidator =
                new JwtTimestampValidator(
                        Duration.ofSeconds(60)
                );

        /*
         * 3. AUDIENCE VALIDATION
         *
         * Ensures that the access token is intended for
         * cargo-shipment-api.
         */
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtAudienceValidator();

        /*
         * 4. REQUIRED CLAIM VALIDATION
         *
         * Ensures that our application-required claims are present:
         * sub, iat and exp.
         */
        OAuth2TokenValidator<Jwt> requiredClaimsValidator =
                new JwtRequiredClaimsValidator();

        /*
         * Combines all validation policies.
         *
         * A failure in any validator rejects the token.
         */
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        timestampValidator,
                        audienceValidator,
                        requiredClaimsValidator
                );

        decoder.setJwtValidator(combinedValidator);

        return decoder;
    }
    /**
     * Configures which frontend origins can access this API
     * through a browser-based cross-origin request.
     *
     * The allowed origin is externalized in application.yml
     * so each environment can have its own frontend URL.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:3000}")
            String allowedOriginsProperty
    ) {

        CorsConfiguration configuration = new CorsConfiguration();

        // Only explicitly trusted frontend origins are allowed.
        List<String> allowedOrigins = Arrays.stream(
                        allowedOriginsProperty.split(",")
                )
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        configuration.setAllowedOrigins(allowedOrigins);

        // Restrict HTTP methods to the operations supported by our API.
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        // Allow the headers required for JSON and Bearer-token requests.
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );

        // We use Bearer tokens in the Authorization header,
        // not browser-managed session cookies.
        configuration.setAllowCredentials(false);

        // Allow browsers to cache successful preflight results for 1 hour.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Apply the configuration to our API endpoints.
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
