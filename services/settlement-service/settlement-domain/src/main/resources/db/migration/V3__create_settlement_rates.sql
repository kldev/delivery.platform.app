CREATE TABLE settlement_rates (
      code            VARCHAR(50) PRIMARY KEY,
      name            VARCHAR(100) NOT NULL,
      percentage      NUMERIC(5, 2) NOT NULL,
      active          BOOLEAN NOT NULL DEFAULT TRUE,
      created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT chk_settlement_rates_percentage
          CHECK (percentage >= 0 AND percentage <= 100)
);

INSERT INTO settlement_rates (
    code,
    name,
    percentage
)
VALUES
    ('BASE', 'Base driver rate', 70.00),
    ('NIGHT', 'Night bonus', 5.00),
    ('WEEKEND', 'Weekend bonus', 5.00),
    ('LONG_DISTANCE', 'Long distance bonus', 10.00);