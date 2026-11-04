CREATE OR REPLACE FUNCTION record_delivery_status_change()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO deliveries_status_history (
            delivery_id,
            status,
            previous_status,
            changed_at
        )
        VALUES (
            NEW.id,
            NEW.status,
             OLD.status,
            NOW()
        );
END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER trg_deliveries_status_change
    AFTER UPDATE OF status ON deliveries
    FOR EACH ROW
    EXECUTE FUNCTION record_delivery_status_change();

CREATE OR REPLACE FUNCTION record_delivery_created()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
INSERT INTO deliveries_status_history (
    delivery_id,
    previous_status,
    status,
    changed_at
)
VALUES (
           NEW.id,
           NULL,
           NEW.status,
           NEW.created_at
       );

RETURN NEW;
END;
$$;

CREATE TRIGGER trg_deliveries_created
    AFTER INSERT ON deliveries
    FOR EACH ROW
    EXECUTE FUNCTION record_delivery_created();