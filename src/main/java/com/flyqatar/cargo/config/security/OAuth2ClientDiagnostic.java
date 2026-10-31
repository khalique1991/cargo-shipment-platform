
package com.flyqatar.cargo.config.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class OAuth2ClientDiagnostic {

    @Bean
    public ApplicationRunner inspectOAuth2Client(
            ClientRegistrationRepository repository) {

        return args -> {

            ClientRegistration registration =
                    repository.findByRegistrationId(
                            "cargo-booking-client"
                    );

            if (registration == null) {
                System.out.println(
                        "ERROR: OAuth2 registration not found"
                );
                return;
            }

            String environmentSecret =
                    System.getenv("CARGO_SHIPMENT_CLIENT_SECRET");

            String configuredSecret =
                    registration.getClientSecret();

            System.out.println("========== OAUTH2 CLIENT DIAGNOSTICS ==========");

            System.out.println(
                    "Registration ID: " + registration.getRegistrationId()
            );

            System.out.println(
                    "Client ID: " + registration.getClientId()
            );

            System.out.println(
                    "Grant Type: " + registration.getAuthorizationGrantType()
            );

            System.out.println(
                    "Authentication Method: "
                            + registration.getClientAuthenticationMethod()
            );

            System.out.println(
                    "Token URI: "
                            + registration.getProviderDetails()
                            .getTokenUri()
            );

            System.out.println(
                    "Environment secret present: "
                            + (environmentSecret != null
                            && !environmentSecret.isBlank())
            );

            System.out.println(
                    "Environment secret length: "
                            + (environmentSecret == null
                            ? 0 : environmentSecret.length())
            );

            System.out.println(
                    "Registered secret present: "
                            + (configuredSecret != null
                            && !configuredSecret.isBlank())
            );

            System.out.println(
                    "Registered secret length: "
                            + (configuredSecret == null
                            ? 0 : configuredSecret.length())
            );

            System.out.println(
                    "Registered secret matches environment: "
                            + (environmentSecret != null
                            && environmentSecret.equals(configuredSecret))
            );

            System.out.println("================================================");
        };
    }
}
