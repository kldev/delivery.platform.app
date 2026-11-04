CREATE TABLE ledger_transactions (
             id UUID PRIMARY KEY,
             type VARCHAR(50) NOT NULL,
             reference_type VARCHAR(50) NOT NULL,
             reference_id UUID NOT NULL,
             currency VARCHAR(3) NOT NULL,
             occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
             created_at TIMESTAMP WITH TIME ZONE NOT NULL,

             CONSTRAINT chk_ledger_transactions_type
                 CHECK (type IN (
                                 'DRIVER_SETTLEMENT',
                                 'DRIVER_PAYOUT',
                                 'ADJUSTMENT'
                     )),

             CONSTRAINT chk_ledger_transactions_currency
                 CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT uq_ledger_transactions_reference
        UNIQUE (reference_type, reference_id)
);

CREATE INDEX ix_ledger_transactions_occurred_at
    ON ledger_transactions (occurred_at);

CREATE INDEX ix_ledger_transactions_type
    ON ledger_transactions (type);