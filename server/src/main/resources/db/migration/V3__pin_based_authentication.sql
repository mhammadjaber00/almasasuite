-- Migration to PIN-based authentication with roles

-- Drop the old users table and recreate with new structure
DROP TABLE IF EXISTS users CASCADE;

-- Create new users table with PIN-based authentication and roles
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pin VARCHAR(255) NOT NULL, -- Hashed PIN
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'manager', 'staff')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    created_by VARCHAR(36) -- Admin who created this user
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_pin ON users(pin);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- Insert default admin user (PIN: 0000)
-- Password hash for PIN "0000" using BCrypt
INSERT INTO users (id, name, pin, role, is_active, created_at, updated_at, created_by)
VALUES (
    'admin-user-id',
    'System Administrator',
    '$2a$12$k8Y6Ri8AYMQkBCqhZxXyT.6JVNiQGQjlV7rqagcbGLYnvHXUQlGAe', -- BCrypt hash for "0000"
    'admin',
    TRUE,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    NULL
) ON CONFLICT (id) DO NOTHING;

-- Insert default manager user (PIN: 1111)
INSERT INTO users (id, name, pin, role, is_active, created_at, updated_at, created_by)
VALUES (
    'manager-user-id',
    'Store Manager',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBmdj6Q9luFiG6', -- BCrypt hash for "1111"
    'manager',
    TRUE,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    'admin-user-id'
) ON CONFLICT (id) DO NOTHING;

-- Insert default staff user (PIN: 2222)
INSERT INTO users (id, name, pin, role, is_active, created_at, updated_at, created_by)
VALUES (
    'staff-user-id',
    'Sales Staff',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- BCrypt hash for "2222"
    'staff',
    TRUE,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    'admin-user-id'
) ON CONFLICT (id) DO NOTHING;