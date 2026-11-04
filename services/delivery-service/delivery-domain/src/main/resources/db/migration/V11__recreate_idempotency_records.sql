-- Idempotency records are short-lived; the old rows are not migrated.
-- Key is scoped per endpoint, the row is written in the same transaction as the business change,
-- so there is no IN_PROGRESS state: a concurrent request waits on the primary key instead.
DROP TABLE idempotency_records;

CREATE TABLE idempotency_records
(
    scope           VARCHAR(200) NOT NULL,
    idempotency_key UUID         NOT NULL,
    request_hash    CHAR(64)     NOT NULL,
    response_status INTEGER      NOT NULL,
    response_body   TEXT         NOT NULL,
    location        VARCHAR(500) NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    expires_at      TIMESTAMPTZ  NOT NULL,

    PRIMARY KEY (scope, idempotency_key)
);

CREATE INDEX idx_idempotency_records_expires_at
    ON idempotency_records (expires_at);
