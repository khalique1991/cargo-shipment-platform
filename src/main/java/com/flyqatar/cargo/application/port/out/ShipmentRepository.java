package com.flyqatar.cargo.application.port.out;

import com.flyqatar.cargo.domain.model.Shipment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(UUID shipmentId);

    List<Shipment> findRecent(int page, int size);

    boolean existsByAwbNumber(String awbNumber);
}
