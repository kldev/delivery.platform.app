CREATE TABLE background_task_stats (
           id UUID PRIMARY KEY,
           task_name VARCHAR(100) NOT NULL UNIQUE,

           executed_times BIGINT NOT NULL DEFAULT 0,

           last_execution_at TIMESTAMPTZ,
           last_execution_duration_ms BIGINT,

           last_success_at TIMESTAMPTZ,
           last_error TEXT,

           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
