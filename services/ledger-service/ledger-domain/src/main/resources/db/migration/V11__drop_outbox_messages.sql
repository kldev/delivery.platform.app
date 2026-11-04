-- The ledger publishes no events: nothing ever wrote to the outbox, its publisher only polled an empty table.
DROP TABLE outbox_messages;
