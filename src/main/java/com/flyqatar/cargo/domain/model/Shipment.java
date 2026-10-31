package com.flyqatar.cargo.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Shipment(
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

    public Shipment {
        Objects.requireNonNull(
                shipmentId,
                "Shipment ID is required"
        );

        Objects.requireNonNull(
                grossWeightKg,
                "Gross weight is required"
        );

        Objects.requireNonNull(
                status,
                "Shipment status is required"
        );

        Objects.requireNonNull(
                createdAt,
                "Creation time is required"
        );

        Objects.requireNonNull(
                updatedAt,
                "Update time is required"
        );

        if (awbNumber == null || awbNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "AWB number is required"
            );
        }

        if (originCode == null || destinationCode == null) {
            throw new IllegalArgumentException(
                    "Origin and destination are required"
            );
        }

        if (originCode.equals(destinationCode)) {
            throw new IllegalArgumentException(
                    "Origin and destination must differ"
            );
        }

        if (grossWeightKg.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Gross weight must be positive"
            );
        }

        if (pieceCount <= 0) {
            throw new IllegalArgumentException(
                    "Piece count must be positive"
            );
        }
    }
}