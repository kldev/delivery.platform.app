-- The reconciliation service publishes no events: nothing ever wrote to the outbox, its publisher only polled.
DROP TABLE outbox_messages;
