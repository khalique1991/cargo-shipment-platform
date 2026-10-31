package com.flyqatar.cargo.adapter.out.integration.security;


import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Automatically attaches an OAuth2 access token
 * to outbound HTTP requests.
 *
 * The token is obtained using the configured
 * OAuth2 Client Credentials flow.
 */
@Component
public class OAuth2BearerTokenInterceptor
        implements ClientHttpRequestInterceptor {

    private final OAuth2AccessTokenProvider accessTokenProvider;

    public OAuth2BearerTokenInterceptor(
            OAuth2AccessTokenProvider accessTokenProvider) {

        this.accessTokenProvider = accessTokenProvider;
    }

    @Override
    public ClientHttpResponse intercept(
            org.springframework.http.HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        // Obtain a valid service-account access token.
        String accessToken = accessTokenProvider.getAccessToken();

        // Attach the token to the outbound request.
        request.getHeaders().setBearerAuth(accessToken);

        // Continue the HTTP request execution.
        return execution.execute(request, body);
    }
}