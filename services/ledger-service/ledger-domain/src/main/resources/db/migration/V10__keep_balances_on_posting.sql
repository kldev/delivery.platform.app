-- Balances are now updated in the posting transaction (JdbcLedgerBalances) instead of a periodic full rebuild.
-- Rebuild once so every account, including driver accounts created before, has a correct row
-- (also applies the direction fix of V9). The procedure stays for manual repair.
CALL rebuild_ledger_account_balances();
