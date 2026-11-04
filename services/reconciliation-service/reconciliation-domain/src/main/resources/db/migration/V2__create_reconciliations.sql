CREATE TABLE reconciliations
(
    id                       UUID PRIMARY KEY,

    delivery_id              UUID    NOT NULL,
    settlement_id            UUID,
    payment_id               UUID,

    external_transaction_id  VARCHAR(255),

    expected_amount          NUMERIC(19, 4),
    actual_amount            NUMERIC(19, 4),
    difference               NUMERIC(19, 4),

    currency                 VARCHAR(3)   NOT NULL,

    status                   VARCHAR(32)  NOT NULL,

    created_at               TIMESTAMPTZ  NOT NULL,
    reconciled_at            TIMESTAMPTZ,

    CONSTRAINT uq_reconciliations_delivery_id
            UNIQUE (delivery_id)
);

CREATE UNIQUE INDEX uq_reconciliations_settlement_id
    ON reconciliations (settlement_id)
    WHERE settlement_id IS NOT NULL;

CREATE UNIQUE INDEX ux_reconciliations_payment_id
    ON reconciliations (payment_id)
    WHERE payment_id IS NOT NULL;

CREATE INDEX ix_reconciliations_status
    ON reconciliations (status);

CREATE INDEX ix_reconciliations_external_transaction_id
    ON reconciliations (external_transaction_id);