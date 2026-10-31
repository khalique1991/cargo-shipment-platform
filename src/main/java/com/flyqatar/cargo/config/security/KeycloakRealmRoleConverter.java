package com.flyqatar.cargo.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class KeycloakRealmRoleConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter scopeConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Collection<GrantedAuthority> authorities =
                new ArrayList<>();

        Collection<GrantedAuthority> scopeAuthorities =
                scopeConverter.convert(jwt);

        if (scopeAuthorities != null) {
            authorities.addAll(scopeAuthorities);
        }

        Map<String, Object> realmAccess =
                jwt.getClaimAsMap("realm_access");

        if (realmAccess == null) {
            return authorities;
        }

        Object rolesObject = realmAccess.get("roles");

        if (rolesObject instanceof Collection<?> roles) {

            for (Object role : roles) {

                if (role instanceof String roleName) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + roleName
                            )
                    );
                }
            }
        }

        return authorities;
    }
}