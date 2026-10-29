CREATE TABLE shipments (
    shipment_id UUID PRIMARY KEY,

    awb_number VARCHAR(32) NOT NULL,

    origin_code VARCHAR(3) NOT NULL,

    destination_code VARCHAR(3) NOT NULL,

    gross_weight_kg NUMERIC(12, 3) NOT NULL,

    piece_count INTEGER NOT NULL,

    status VARCHAR(32) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_shipments_awb
        UNIQUE (awb_number),

    CONSTRAINT chk_shipments_origin_format
        CHECK (origin_code ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_shipments_destination_format
        CHECK (destination_code ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_shipments_different_airports
        CHECK (origin_code <> destination_code),

    CONSTRAINT chk_shipments_positive_weight
        CHECK (gross_weight_kg > 0),

    CONSTRAINT chk_shipments_positive_piece_count
        CHECK (piece_count > 0),

    CONSTRAINT chk_shipments_valid_status
        CHECK (
            status IN (
                'CREATED',
                'BOOKED',
                'ACCEPTED',
                'IN_TRANSIT',
                'ARRIVED',
                'DELIVERED',
                'ON_HOLD',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_shipments_created_at_id
    ON shipments (created_at DESC, shipment_id DESC);