-- recipient had two meanings in one VARCHAR(255): the API stored a plain address, the sender stored a JSON
-- object, and the sender always parsed it as JSON. It is now always a JSON object {fullName, phoneNumber, email}.
-- Existing plain values become {"email": value}.
DROP INDEX IF EXISTS idx_notifications_recipient;

ALTER TABLE notifications
    ALTER COLUMN recipient TYPE JSONB
    USING CASE
              WHEN recipient IS NULL OR btrim(recipient) = '' THEN NULL
              WHEN left(btrim(recipient), 1) = '{' THEN recipient::jsonb
              ELSE jsonb_build_object('email', recipient)
          END;

-- When a notification was claimed: a PROCESSING row older than the lease belongs to a run that died and can be
-- claimed again. Rows already stuck in PROCESSING get a claim time so the next run picks them up.
ALTER TABLE notifications ADD COLUMN claimed_at TIMESTAMPTZ NULL;
UPDATE notifications SET claimed_at = created_at WHERE status = 'PROCESSING';
