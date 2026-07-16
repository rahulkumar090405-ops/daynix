ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS customer_id UUID;

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS customer_id UUID;

ALTER TABLE time_slots
    ADD COLUMN IF NOT EXISTS customer_id UUID;

ALTER TABLE categories
    ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE tasks
    ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE time_slots
    ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE categories
    DROP CONSTRAINT IF EXISTS uq_categories_name;

ALTER TABLE categories
    ADD CONSTRAINT uq_categories_customer_name UNIQUE (customer_id, name);

ALTER TABLE time_slots
    DROP CONSTRAINT IF EXISTS uq_time_slots_display_order;

ALTER TABLE time_slots
    ADD CONSTRAINT uq_time_slots_customer_display_order UNIQUE (customer_id, display_order);

ALTER TABLE categories
    ADD CONSTRAINT fk_categories_customer_id FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_customer_id FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE time_slots
    ADD CONSTRAINT fk_time_slots_customer_id FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_categories_customer_id ON categories(customer_id);
CREATE INDEX IF NOT EXISTS idx_tasks_customer_id ON tasks(customer_id);
CREATE INDEX IF NOT EXISTS idx_time_slots_customer_id ON time_slots(customer_id);