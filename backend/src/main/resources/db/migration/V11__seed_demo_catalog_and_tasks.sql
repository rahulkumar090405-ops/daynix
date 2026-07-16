INSERT INTO categories (id, customer_id, name, description, color, created_at, updated_at, deleted, version)
VALUES
    ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'Work', 'Work-related tasks', '#2F80ED', NOW(), NOW(), FALSE, 0),
    ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'Personal', 'Personal and household tasks', '#27AE60', NOW(), NOW(), FALSE, 0),
    ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 'Health', 'Fitness and wellness tasks', '#EB5757', NOW(), NOW(), FALSE, 0),
    ('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002', 'Learning', 'Study and skill-building tasks', '#F2994A', NOW(), NOW(), FALSE, 0)
ON CONFLICT (customer_id, name) DO NOTHING;

INSERT INTO time_slots (id, customer_id, start_time, end_time, display_order, active, interval_minutes, created_at, updated_at, deleted, version)
VALUES
    ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '08:00:00', '09:00:00', 1, TRUE, 60, NOW(), NOW(), FALSE, 0),
    ('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', '09:00:00', '09:30:00', 2, TRUE, 30, NOW(), NOW(), FALSE, 0),
    ('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', '09:30:00', '10:00:00', 3, TRUE, 30, NOW(), NOW(), FALSE, 0),
    ('30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002', '10:00:00', '11:00:00', 4, TRUE, 60, NOW(), NOW(), FALSE, 0),
    ('30000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000002', '14:00:00', '14:15:00', 5, TRUE, 15, NOW(), NOW(), FALSE, 0),
    ('30000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000002', '16:00:00', '17:00:00', 6, TRUE, 60, NOW(), NOW(), FALSE, 0)
ON CONFLICT (customer_id, display_order) DO NOTHING;

INSERT INTO tasks (id, customer_id, title, description, category_id, priority, estimated_minutes, color, active_status, created_at, updated_at, deleted, version)
VALUES
    ('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'Plan tomorrow''s work', 'Review priorities and prepare the next day', '20000000-0000-0000-0000-000000000001', 'HIGH', 30, '#2F80ED', TRUE, NOW(), NOW(), FALSE, 0),
    ('40000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'Workout session', 'Complete a 45 minute workout', '20000000-0000-0000-0000-000000000003', 'MEDIUM', 45, '#EB5757', TRUE, NOW(), NOW(), FALSE, 0),
    ('40000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 'Read one chapter', 'Spend time on the current learning goal', '20000000-0000-0000-0000-000000000004', 'LOW', 20, '#F2994A', TRUE, NOW(), NOW(), FALSE, 0)
ON CONFLICT (id) DO NOTHING;