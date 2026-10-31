package com.flyqatar.cargo.config;

import com.flyqatar.cargo.adapter.out.integration.security
        .OAuth2BearerTokenInterceptor;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Creates an HTTP client with a connection timeout.
     */
    @Bean
    public HttpClient bookingHttpClient() {

        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Configures the HTTP request factory.
     *
     * Connection timeout: 3 seconds.
     * Response/read timeout: 10 seconds.
     */
    @Bean
    public JdkClientHttpRequestFactory bookingRequestFactory(
            HttpClient bookingHttpClient) {

        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(bookingHttpClient);

        factory.setReadTimeout(Duration.ofSeconds(10));

        return factory;
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Dedicated authenticated HTTP client for Booking integration.
     */
    @Bean
    public RestClient bookingRestClient(
            RestClient.Builder builder,
            OAuth2BearerTokenInterceptor tokenInterceptor,
            JdkClientHttpRequestFactory bookingRequestFactory) {

        return builder
                .baseUrl("http://localhost:8080")
                .requestFactory(bookingRequestFactory)
                .requestInterceptor(tokenInterceptor)
                .build();
    }
}