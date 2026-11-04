ALTER TABLE drivers
    ADD COLUMN email VARCHAR(320);

CREATE UNIQUE INDEX ux_drivers_email
    ON drivers (email)
    WHERE email IS NOT NULL;