CREATE TABLE idempotency_records (
         id UUID PRIMARY KEY,

         idempotency_key UUID NOT NULL,
         status VARCHAR(20) NOT NULL,
         request_hash VARCHAR(64) NOT NULL,

         response_status INTEGER NULL,
         response_body TEXT NULL,

         created_at TIMESTAMP WITH TIME ZONE NOT NULL,
         expires_at TIMESTAMP WITH TIME ZONE,
         completed_at TIMESTAMP WITH TIME ZONE,


         CONSTRAINT uk_idempotency_key
             UNIQUE (idempotency_key)
);

CREATE INDEX idx_idempotency_records_expires_at
    ON idempotency_records (expires_at);
