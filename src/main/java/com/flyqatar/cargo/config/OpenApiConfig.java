
package com.flyqatar.cargo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String OAUTH_SCHEME = "keycloakOAuth";

    @Bean
    public OpenAPI cargoShipmentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cargo Shipment Platform API")
                        .version("v1")
                        .description(
                                "REST APIs for creating and tracking cargo shipments."
                        ))
                .components(new Components()
                        .addSecuritySchemes(
                                OAUTH_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                                                .authorizationCode(
                                                        new io.swagger.v3.oas.models.security.OAuthFlow()
                                                                .authorizationUrl(
                                                                        "http://localhost:8081/realms/cargo-platform/protocol/openid-connect/auth"
                                                                )
                                                                .tokenUrl(
                                                                        "http://localhost:8081/realms/cargo-platform/protocol/openid-connect/token"
                                                                )
                                                                .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                                        .addString("openid", "Authenticate user")
                                                                        .addString("profile", "Read user profile")
                                                                        .addString("email", "Read email")
                                                                )
                                                )
                                        )
                        ))
                .addSecurityItem(
                        new SecurityRequirement().addList(OAUTH_SCHEME)
                );
    }
}
