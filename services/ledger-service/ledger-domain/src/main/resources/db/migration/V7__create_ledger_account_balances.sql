CREATE TABLE ledger_account_balances (
             account_id UUID PRIMARY KEY,
             currency VARCHAR(3) NOT NULL,
             balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
             updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

             CONSTRAINT fk_ledger_account_balances_account
                 FOREIGN KEY (account_id)
                     REFERENCES ledger_accounts (id),

             CONSTRAINT chk_ledger_account_balances_currency
                 CHECK (currency ~ '^[A-Z]{3}$')
    );

CREATE INDEX ix_ledger_account_balances_currency
    ON ledger_account_balances (currency);

INSERT INTO ledger_account_balances (
    account_id,
    currency,
    balance,
    updated_at
)
SELECT
    id,
    currency,
    0,
    CURRENT_TIMESTAMP
FROM ledger_accounts
WHERE owner_type = 'PLATFORM'