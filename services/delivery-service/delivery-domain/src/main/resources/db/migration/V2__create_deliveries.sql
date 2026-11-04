CREATE TABLE deliveries
(
    id               UUID PRIMARY KEY,
    driver_id        UUID         NULL,
    pickup_address   VARCHAR(500) NOT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    price            NUMERIC(12, 2) NOT NULL,
    distance_km      numeric(12,4) NOT NULL,
    status           VARCHAR(30)  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NULL,

    FOREIGN KEY (driver_id) REFERENCES drivers(id),
    CONSTRAINT chk_deliveries_status
        CHECK (
            status IN (
                       'CREATED',
                       'CONFIRMED',
                       'ASSIGNED',
                       'PICKED_UP',
                       'IN_TRANSIT',
                       'DELIVERED',
                       'CANCELLED'
                )
            )
);

CREATE INDEX idx_deliveries_driver_id
    ON deliveries (driver_id) where NOT NULL;

CREATE INDEX idx_deliveries_status
    ON deliveries (status);

