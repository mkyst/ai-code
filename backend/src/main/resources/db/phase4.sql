-- Phase 4: Add status column to users table
USE ai_code;

ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT 'user status: active/banned';

-- Ensure admin user has admin role
UPDATE users SET role = 'admin' WHERE email = 'admin@aicode.com';

