ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- Create a default admin user if one doesn't exist
-- Password is 'admin' (BCrypt hashed)
INSERT INTO users (phonenumber, password, username, role, status)
VALUES ('0000000000', '$2a$10$8.UnVuG9HLROJQQIgvOq8.GOMqE./10T0V/Qn/J5lRIfqgqgR1H4O', 'Super Admin', 'ADMIN', 'ACTIVE')
ON CONFLICT (phonenumber) DO UPDATE SET role = 'ADMIN';
