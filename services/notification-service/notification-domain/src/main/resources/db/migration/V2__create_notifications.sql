CREATE TABLE notifications (
       id UUID PRIMARY KEY,
       event_id UUID NOT NULL,
       event_type VARCHAR(255) NOT NULL,
       recipient VARCHAR(255) NULL,
       channel VARCHAR(50) NOT NULL,
       payload JSONB NOT NULL,
       status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
       attempts INTEGER NOT NULL DEFAULT 0,
       last_error TEXT,
       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
       sent_at TIMESTAMPTZ,
       CONSTRAINT uk_notifications_event_channel
           UNIQUE (event_id, channel)
);

-- Queue processing: fetch pending notifications
CREATE INDEX idx_notifications_pending_created_at
    ON notifications (created_at, id)
    WHERE status = 'PENDING';

-- Useful for querying notifications for a recipient
CREATE INDEX idx_notifications_recipient
    ON notifications (recipient);

-- Useful for filtering by status and ordering
CREATE INDEX idx_notifications_status_created_at
    ON notifications (status, created_at);

-- Useful for querying event type
CREATE INDEX idx_notifications_event_type
    ON notifications (event_type);