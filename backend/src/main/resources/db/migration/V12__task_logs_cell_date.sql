ALTER TABLE task_logs
    ADD COLUMN IF NOT EXISTS task_date DATE;

UPDATE task_logs
SET task_date = COALESCE(task_date, logged_at::date)
WHERE task_date IS NULL;

ALTER TABLE task_logs
    ALTER COLUMN task_date SET NOT NULL;

ALTER TABLE task_logs
    DROP CONSTRAINT IF EXISTS uq_task_logs_task_date;

ALTER TABLE task_logs
    ADD CONSTRAINT uq_task_logs_task_date UNIQUE (task_id, task_date);

CREATE INDEX IF NOT EXISTS idx_task_logs_task_date ON task_logs(task_date);