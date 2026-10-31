package com.flyqatar.cargo.application.service;


import com.flyqatar.cargo.application.port.out.ShipmentRepository;
import com.flyqatar.cargo.application.service.exception.DuplicateShipmentException;
import com.flyqatar.cargo.application.service.exception.ShipmentNotFoundException;
import com.flyqatar.cargo.domain.model.Shipment;
import com.flyqatar.cargo.domain.model.ShipmentStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ShipmentApplicationService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentApplicationService(
            ShipmentRepository shipmentRepository
    ) {
        this.shipmentRepository = shipmentRepository;
    }
    @PreAuthorize("hasRole('SHIPMENT_CREATE')")
    @Transactional
    public Shipment createShipment(
            String awbNumber,
            String originCode,
            String destinationCode,
            BigDecimal grossWeightKg,
            int pieceCount
    ) {

        String normalizedAwb = normalizeAwb(awbNumber);
        String normalizedOrigin = normalizeAirport(originCode);
        String normalizedDestination =
                normalizeAirport(destinationCode);

        if (shipmentRepository.existsByAwbNumber(normalizedAwb)) {
            throw new DuplicateShipmentException(
                    "A shipment with this AWB already exists"
            );
        }

        Instant now = Instant.now();

        Shipment shipment = new Shipment(
                UUID.randomUUID(),
                normalizedAwb,
                normalizedOrigin,
                normalizedDestination,
                grossWeightKg,
                pieceCount,
                ShipmentStatus.CREATED,
                now,
                now
        );

        return shipmentRepository.save(shipment);
    }
    @PreAuthorize("hasRole('SHIPMENT_READ')")
    @Transactional(readOnly = true)
    public Shipment getShipment(UUID shipmentId) {

        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new ShipmentNotFoundException(
                                "Shipment not found"
                        )
                );
    }
    @PreAuthorize("hasRole('SHIPMENT_READ')")
    @Transactional(readOnly = true)
    public List<Shipment> listShipments(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        long calculatedOffset = (long) page * size;

        if (calculatedOffset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Requested page is too large"
            );
        }

        return shipmentRepository.findRecent(
                (int) calculatedOffset,
                size
        );
    }
    private String normalizeAwb(String awbNumber) {

        if (awbNumber == null || awbNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "AWB number is required"
            );
        }

        return awbNumber.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeAirport(String airportCode) {

        if (airportCode == null || airportCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Airport code is required"
            );
        }

        return airportCode.trim().toUpperCase(Locale.ROOT);
    }



}