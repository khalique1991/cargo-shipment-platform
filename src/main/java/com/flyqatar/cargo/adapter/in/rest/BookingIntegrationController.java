package com.flyqatar.cargo.adapter.in.rest;

import com.flyqatar.cargo.adapter.in.rest.dto.ServiceIntegrationResponse;
import com.flyqatar.cargo.application.service.BookingIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integrations")
public class BookingIntegrationController {

    private final BookingIntegrationService integrationService;

    public BookingIntegrationController(
            BookingIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/booking/health")
    public ResponseEntity<ServiceIntegrationResponse> checkBookingIntegration() {

        String downstreamResponse =
                integrationService.checkBookingService();

        return ResponseEntity.ok(
                new ServiceIntegrationResponse(
                        "cargo-shipment-service",
                        "SUCCESS",
                        "OAuth2 authenticated outbound request completed",
                        downstreamResponse
                )
        );
    }
}