CREATE TABLE task_time_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    time_slot_id UUID NOT NULL REFERENCES time_slots(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_task_time_mapping_task_time_slot UNIQUE (task_id, time_slot_id)
);

CREATE INDEX idx_task_time_mapping_task_id ON task_time_mapping(task_id);
CREATE INDEX idx_task_time_mapping_time_slot_id ON task_time_mapping(time_slot_id);