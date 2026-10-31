
package com.flyqatar.cargo.config.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;


/**
 * Validates mandatory JWT claims required by Cargo Shipment API.
 *
 * WHY:
 * A JWT can be correctly signed and issued by a trusted issuer
 * while still failing our application's token requirements.
 *
 * PROBLEM SOLVED:
 * Ensures every accepted user access token has an identifiable
 * subject and the timestamps required by our token contract.
 *
 * WITHOUT THIS VALIDATOR:
 * Tokens missing subject or required timestamps could pass
 * signature and issuer validation if those claims were optional
 * in the token profile.
 *
 * IMPORTANT:
 * Expiration and not-before timestamp comparisons are performed
 * separately by JwtTimestampValidator.
 */
public class JwtRequiredClaimsValidator
        implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        // Subject identifies the user or principal represented
        // by the token. We require it for audit and attribution.
        if (jwt.getSubject() == null ||
                jwt.getSubject().isBlank()) {

            return failure(
                    "JWT subject (sub) is missing or empty"
            );
        }

        // Issued-at is required by our Cargo API token contract.
        // It provides the issuance timestamp for security auditing.
        if (jwt.getIssuedAt() == null) {

            return failure(
                    "JWT issued-at (iat) claim is missing"
            );
        }

        // Expiration must be present. JwtTimestampValidator
        // separately checks whether the token has expired.
        if (jwt.getExpiresAt() == null) {

            return failure(
                    "JWT expiration (exp) claim is missing"
            );
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult failure(String description) {

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                description,
                null
        );

        return OAuth2TokenValidatorResult.failure(error);
    }
}
