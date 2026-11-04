CREATE TABLE deliveries_status_history (
           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

           delivery_id UUID NOT NULL,
           previous_status VARCHAR(50) NULL,
           status VARCHAR(50) NOT NULL,

           changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

           CONSTRAINT fk_deliveries_status_history_delivery
               FOREIGN KEY (delivery_id)
                   REFERENCES deliveries(id)
                   ON DELETE CASCADE
);

CREATE INDEX idx_deliveries_status_history_delivery_id
    ON deliveries_status_history(delivery_id);

CREATE INDEX idx_deliveries_status_history_delivery_changed_at
    ON deliveries_status_history(delivery_id, changed_at);