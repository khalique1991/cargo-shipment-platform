package com.flyqatar.cargo.adapter.in.rest;

import com.flyqatar.cargo.adapter.in.rest.dto.CreateShipmentRequest;
import com.flyqatar.cargo.adapter.in.rest.dto.ShipmentPageResponse;
import com.flyqatar.cargo.adapter.in.rest.dto.ShipmentResponse;
import com.flyqatar.cargo.application.service.ShipmentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@Tag(
        name = "Shipments",
        description = "Create and retrieve cargo shipments"
)
public class ShipmentController {

    private final ShipmentApplicationService shipmentService;

    public ShipmentController(
            ShipmentApplicationService shipmentService
    ) {
        this.shipmentService = shipmentService;
    }
    @Operation(
            summary = "Create a shipment",
            description = "Validates and creates a cargo shipment."
    )
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request
    ) {

        ShipmentResponse response = ShipmentResponse.from(
                shipmentService.createShipment(
                        request.awbNumber(),
                        request.originCode(),
                        request.destinationCode(),
                        request.grossWeightKg(),
                        request.pieceCount()
                )
        );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/shipments/"
                                        + response.shipmentId()
                        )
                )
                .body(response);
    }
    @Operation(
            summary = "Get a shipment",
            description = "Retrieves a shipment by its identifier."
    )
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(
            @PathVariable UUID shipmentId
    ) {

        ShipmentResponse response = ShipmentResponse.from(
                shipmentService.getShipment(shipmentId)
        );

        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "List shipments",
            description = "Returns a paginated list of shipments."
    )
    @GetMapping
    public ResponseEntity<ShipmentPageResponse<ShipmentResponse>>
    listShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        var shipments = shipmentService.listShipments(page, size)
                .stream()
                .map(ShipmentResponse::from)
                .toList();

        return ResponseEntity.ok(
                new ShipmentPageResponse<>(
                        shipments,
                        page,
                        size
                )
        );
    }
}