-- Driver settlements were posted the wrong way round (platform CREDIT, driver DEBIT), so a driver the platform
-- owes money to had a negative balance. Flip the entries of every DRIVER_SETTLEMENT already posted; the
-- transactions stay balanced because both entries flip. Balances are rebuilt in V10.
UPDATE ledger_entries e
SET type = CASE e.type WHEN 'DEBIT' THEN 'CREDIT' ELSE 'DEBIT' END
FROM ledger_transactions t
WHERE e.transaction_id = t.id
  AND t.type = 'DRIVER_SETTLEMENT';
