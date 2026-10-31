package com.flyqatar.cargo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central Jackson configuration.
 *
 * Registers Java Time support so that Java date/time
 * objects such as Instant can be serialized correctly.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {

        ObjectMapper mapper = new ObjectMapper();

        // Enables serialization of Instant, LocalDateTime,
        // OffsetDateTime and other Java Time API types.
        mapper.registerModule(new JavaTimeModule());

        // Produces readable ISO-8601 timestamps instead of
        // numeric timestamp representations.
        mapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        return mapper;
    }
}