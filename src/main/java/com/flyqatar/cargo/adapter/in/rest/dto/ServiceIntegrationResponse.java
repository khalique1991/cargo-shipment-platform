package com.flyqatar.cargo.adapter.in.rest.dto;

public record ServiceIntegrationResponse(
        String serviceName,
        String integrationStatus,
        String message,
        String downstreamResponse
) {
}