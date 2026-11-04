CREATE TABLE ledger_accounts (
             id UUID PRIMARY KEY,
             owner_type VARCHAR(50) NOT NULL,
             owner_id UUID,
             currency VARCHAR(3) NOT NULL,
             created_at TIMESTAMP WITH TIME ZONE NOT NULL,
             name VARCHAR(200) NULL,

             CONSTRAINT chk_ledger_accounts_owner_type
                 CHECK (owner_type IN ('PLATFORM', 'DRIVER')),

             CONSTRAINT chk_ledger_accounts_owner_id
                 CHECK (
                     (owner_type = 'PLATFORM' AND owner_id IS NULL)
                         OR
                     (owner_type = 'DRIVER' AND owner_id IS NOT NULL)
                     ),

             CONSTRAINT chk_ledger_accounts_currency
                 CHECK (currency ~ '^[A-Z]{3}$')
    );

CREATE UNIQUE INDEX ux_ledger_accounts_owner_currency
    ON ledger_accounts (owner_type, owner_id, currency);