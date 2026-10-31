package com.flyqatar.cargo.config.security;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {

        return new InMemoryOAuth2AuthorizedClientService(
                clientRegistrationRepository);
    }

    /**
     * Dedicated HTTP client for Keycloak token requests.
     * This client must not contain our outbound OAuth2 interceptor.
     */
    @Bean
    public RestClient keycloakTokenRestClient() {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(requestFactory)

                // Spring Framework 7 recommended API.
                .configureMessageConverters(converters -> converters
                        .registerDefaults()
                        .addCustomConverter(
                                new FormHttpMessageConverter())
                        .addCustomConverter(
                                new OAuth2AccessTokenResponseHttpMessageConverter())
                )
                .defaultStatusHandler(
                        new OAuth2ErrorResponseErrorHandler())
                .build();
    }
    /**
     * Configures Client Credentials token acquisition
     * with clock skew and a custom token endpoint client.
     */
    @Bean
    public OAuth2AuthorizedClientProvider authorizedClientProvider(
            RestClient keycloakTokenRestClient) {

        RestClientClientCredentialsTokenResponseClient tokenResponseClient =
                new RestClientClientCredentialsTokenResponseClient();

        tokenResponseClient.setRestClient(keycloakTokenRestClient);

        ClientCredentialsOAuth2AuthorizedClientProvider provider =
                new ClientCredentialsOAuth2AuthorizedClientProvider();

        provider.setAccessTokenResponseClient(tokenResponseClient);
        provider.setClockSkew(Duration.ofSeconds(60));

        return provider;
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            OAuth2AuthorizedClientProvider authorizedClientProvider) {

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService);

        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }
}