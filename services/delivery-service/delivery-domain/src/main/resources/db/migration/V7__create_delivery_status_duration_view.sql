CREATE VIEW delivery_status_duration AS
SELECT
    id,
    delivery_id,
    previous_status,
    status,
    changed_at AS status_from,

    LEAD(changed_at) OVER (
        PARTITION BY delivery_id
        ORDER BY changed_at
    ) AS status_to,

    EXTRACT(
            EPOCH FROM (
        COALESCE(
                LEAD(changed_at) OVER (
                PARTITION BY delivery_id
                    ORDER BY changed_at
                                 ),
                NOW()
        ) - changed_at
        )
    ) AS duration_seconds

FROM deliveries_status_history
ORDER BY id ASC, changed_at ASC;