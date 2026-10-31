package com.flyqatar.cargo.adapter.in.rest.dto;


import com.flyqatar.cargo.domain.model.Shipment;
import com.flyqatar.cargo.domain.model.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(
        UUID shipmentId,
        String awbNumber,
        String originCode,
        String destinationCode,
        BigDecimal grossWeightKg,
        int pieceCount,
        ShipmentStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ShipmentResponse from(Shipment shipment) {

        return new ShipmentResponse(
                shipment.shipmentId(),
                shipment.awbNumber(),
                shipment.originCode(),
                shipment.destinationCode(),
                shipment.grossWeightKg(),
                shipment.pieceCount(),
                shipment.status(),
                shipment.createdAt(),
                shipment.updatedAt()
        );
    }
}