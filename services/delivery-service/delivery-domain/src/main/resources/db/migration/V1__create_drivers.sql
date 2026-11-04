CREATE TABLE drivers
(
    id           UUID PRIMARY KEY,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30)  NOT NULL,
    status       VARCHAR(30)  NOT NULL,

    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NULL,

    CONSTRAINT uk_drivers_phone_number
        UNIQUE (phone_number),

    CONSTRAINT chk_drivers_status
        CHECK (status IN (
                          'ACTIVE',
                          'INACTIVE',
                          'SUSPENDED'
            ))
);

CREATE INDEX idx_drivers_status
    ON drivers (status);