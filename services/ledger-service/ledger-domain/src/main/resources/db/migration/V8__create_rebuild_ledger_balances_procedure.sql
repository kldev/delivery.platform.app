CREATE OR REPLACE PROCEDURE rebuild_ledger_account_balances()
LANGUAGE plpgsql
AS $$
BEGIN
TRUNCATE TABLE ledger_account_balances;

INSERT INTO ledger_account_balances (
    account_id,
    currency,
    balance,
    updated_at
)
SELECT
    a.id,
    a.currency,
    COALESCE(
            SUM(
                    CASE
                        WHEN e.type = 'CREDIT' THEN e.amount
                        WHEN e.type = 'DEBIT' THEN -e.amount
                        ELSE 0
                        END
            ),
            0
    ),
    CURRENT_TIMESTAMP
FROM ledger_accounts a
         LEFT JOIN ledger_entries e
                   ON e.account_id = a.id
GROUP BY
    a.id,
    a.currency;
END;
$$;