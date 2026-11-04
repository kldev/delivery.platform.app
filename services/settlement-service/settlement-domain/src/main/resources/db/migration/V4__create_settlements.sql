CREATE TABLE settlements (
             id                  UUID PRIMARY KEY,
             delivery_id         UUID NOT NULL,
             driver_id           UUID NOT NULL,
             driver_full_name    VARCHAR(500) NOT NULL,

             delivery_amount     NUMERIC(12, 2) NOT NULL,
             currency             VARCHAR(3) NOT NULL,

             base_percentage      NUMERIC(5, 2) NOT NULL,
             night_percentage     NUMERIC(5, 2) NOT NULL DEFAULT 0,
             weekend_percentage   NUMERIC(5, 2) NOT NULL DEFAULT 0,
             distance_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0,

             total_percentage     NUMERIC(5, 2) NOT NULL,
             driver_amount        NUMERIC(12, 2) NOT NULL,

             distance_km          NUMERIC(10, 2) NOT NULL,
             completed_at         TIMESTAMPTZ NOT NULL,

             created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

             CONSTRAINT uq_settlements_delivery
                 UNIQUE (delivery_id),

             CONSTRAINT chk_settlements_percentage
                 CHECK (total_percentage >= 0 AND total_percentage <= 100),

             CONSTRAINT chk_settlements_amount
                 CHECK (delivery_amount >= 0 AND driver_amount >= 0)
);