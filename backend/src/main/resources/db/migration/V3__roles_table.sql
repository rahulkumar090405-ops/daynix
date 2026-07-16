CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(40) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_roles_code UNIQUE (code),
    CONSTRAINT chk_roles_code CHECK (code IN ('ROLE_SUPER_ADMIN', 'ROLE_CUSTOMER'))
);

INSERT INTO roles (code, display_name, description)
VALUES
    ('ROLE_SUPER_ADMIN', 'Super Admin', 'Full platform administration access'),
    ('ROLE_CUSTOMER', 'Customer', 'Standard customer account')
ON CONFLICT (code) DO NOTHING;