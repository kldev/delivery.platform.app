CREATE TABLE ledger_entries (
            id UUID PRIMARY KEY,
            transaction_id UUID NOT NULL,
            account_id UUID NOT NULL,
            amount NUMERIC(19, 4) NOT NULL,
            type VARCHAR(30) NOT NULL, --
            created_at TIMESTAMP WITH TIME ZONE NOT NULL,

            CONSTRAINT fk_ledger_entries_transaction
                FOREIGN KEY (transaction_id)
                    REFERENCES ledger_transactions (id),

            CONSTRAINT fk_ledger_entries_account
                FOREIGN KEY (account_id)
                    REFERENCES ledger_accounts (id),

            CONSTRAINT chk_ledger_entries_amount
                CHECK (amount <> 0),

            CONSTRAINT ledger_entries_type
                CHECK (type IN (
                                'DEBIT',
                                'CREDIT'
                    ))
);

CREATE INDEX ix_ledger_entries_transaction_id
    ON ledger_entries (transaction_id);

CREATE INDEX ix_ledger_entries_account_id
    ON ledger_entries (account_id);

CREATE INDEX ix_ledger_entries_account_created_at
    ON ledger_entries (account_id, created_at);