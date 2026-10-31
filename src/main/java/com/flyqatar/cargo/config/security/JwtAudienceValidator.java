
package com.flyqatar.cargo.config.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Validates whether a JWT is intended for Cargo Shipment API.
 *
 * WHY:
 * A Keycloak realm can issue tokens for multiple applications.
 * A valid signature and trusted issuer alone do not establish
 * that a token was intended for this particular API.
 *
 * PROBLEM SOLVED:
 * Prevents accepting a token intended for another application
 * merely because it was issued by the same trusted identity provider.
 *
 * WITHOUT THIS VALIDATION:
 * A token issued for another API in the same realm could pass
 * issuer and signature validation if the remaining security
 * requirements were also satisfied.
 *
 * PRODUCTION REQUIREMENT:
 * The expected audience must be configured in Keycloak and
 * included in the access token's "aud" claim.
 */
public class JwtAudienceValidator
        implements OAuth2TokenValidator<Jwt> {

    private static final String REQUIRED_AUDIENCE =
            "cargo-shipment-api";

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        // Read the audience claim from the verified JWT.
        // A token can contain one or multiple audience values.
        if (jwt.getAudience().contains(REQUIRED_AUDIENCE)) {

            // Continue authentication when Cargo API is an intended
            // recipient of this token.
            return OAuth2TokenValidatorResult.success();
        }

        // Reject tokens issued for a different audience.
        // Spring Security will treat this as an invalid bearer token.
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "Token audience does not include cargo-shipment-api",
                null
        );

        return OAuth2TokenValidatorResult.failure(error);
    }
}
