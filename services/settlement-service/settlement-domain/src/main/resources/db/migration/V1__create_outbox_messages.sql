CREATE TABLE outbox_messages (
     id UUID PRIMARY KEY,

     aggregate_id       UUID NOT NULL,
     module VARCHAR(100) NOT NULL,

     event_id UUID NOT NULL,
     event_type VARCHAR(150) NOT NULL,

     payload JSONB NOT NULL,

     status VARCHAR(30) NOT NULL,

     attempts INTEGER NOT NULL DEFAULT 0,

     next_attempt_at TIMESTAMPTZ NOT NULL,

     occurred_at TIMESTAMPTZ NOT NULL,
     created_at TIMESTAMPTZ NOT NULL,

     published_at TIMESTAMPTZ NULL,
     last_error TEXT NULL,

     locked_until TIMESTAMPTZ NULL,

     CONSTRAINT ck_outbox_status
         CHECK (status IN ('PENDING', 'PUBLISHED', 'DEAD')),

     CONSTRAINT ck_outbox_attempts
         CHECK (attempts >= 0)
);

CREATE INDEX idx_outbox_pending
    ON outbox_messages (next_attempt_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_aggregate
    ON outbox_messages (module, aggregate_id);